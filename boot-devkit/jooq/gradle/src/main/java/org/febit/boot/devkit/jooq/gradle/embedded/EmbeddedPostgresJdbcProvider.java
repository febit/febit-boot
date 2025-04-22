/*
 * Copyright 2022-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.boot.devkit.jooq.gradle.embedded;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import io.zonky.test.db.postgres.embedded.PgBinaryResolver;
import io.zonky.test.db.postgres.util.ArchUtils;
import io.zonky.test.db.postgres.util.LinuxUtils;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOptionImpl;
import org.febit.boot.devkit.jooq.gradle.EmbeddedPostgresConfig;
import org.febit.boot.devkit.jooq.gradle.JdbcProvider;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenExtension;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenPlugin;
import org.febit.boot.devkit.jooq.meta.JooqCodegen;
import org.febit.boot.devkit.jooq.meta.embedded.PackageUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.lang.util.Lists;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.lowerCase;

@Slf4j
@lombok.Builder(
        builderClassName = "Builder"
)
public class EmbeddedPostgresJdbcProvider implements JdbcProvider<EmbeddedPostgresJdbcProvider.Params> {

    static final String RUNTIME_NAME_PG = JooqCodegenPlugin.RUNTIME + "EmbeddedPostgres";
    private static final String WORK_DIR = "codegen-embedded-pg";
    private static final String DATA_DIR = "data";
    private static final String POSTGRES = "postgres";

    @Getter
    private final EmbeddedPostgresConfig conf;
    @Getter
    private final File buildDir;

    private final AtomicReference<StartedInstance> startedRef = new AtomicReference<>();

    public static void prepare(Project project) {
        var configs = project.getConfigurations();
        if (configs.findByName(RUNTIME_NAME_PG) != null) {
            return;
        }
        configs.create(RUNTIME_NAME_PG)
                .setDescription("The classpath for codegen to used to load embedded Postgres binaries");
    }

    @Override
    public void afterEvaluate(Project project) {
        var artifact = PackageUtils.Postgres.artifact();
        var extension = project.getExtensions()
                .getByType(JooqCodegenExtension.class);
        var conf = extension.getEmbeddedPostgres();
        var deps = project.getDependencies();

        if (!JooqCodegenPlugin.INTERNAL_TESTING_MODE.get()) {
            var bom = "org.febit.boot:febit-boot-bom:" + JooqCodegen.version();
            deps.add(RUNTIME_NAME_PG, deps.platform(bom));
        }
        deps.add(JooqCodegenPlugin.RUNTIME, "org.postgresql:postgresql");
        deps.add(RUNTIME_NAME_PG, deps.platform(
                "io.zonky.test.postgres:embedded-postgres-binaries-bom:" + conf.getVersion()
        ));
        deps.add(RUNTIME_NAME_PG, artifact);
        log.info("Using embedded postgres: {}, version: {}", artifact, conf.getVersion());
    }

    private File resolveWorkDir() {
        var conf = getConf();
        var workDir = conf.getWorkingDir();
        if (workDir != null) {
            return workDir;
        }
        return new File(buildDir, WORK_DIR);
    }

    private File resolveDataDir() {
        var conf = getConf();
        var dataDir = conf.getDataDir();
        if (dataDir != null) {
            return dataDir;
        }
        return new File(buildDir, WORK_DIR + "/" + DATA_DIR);
    }

    @Override
    public synchronized JdbcOption prepare(Params params) {
        var started = this.startedRef.get();
        if (started != null) {
            return started.option();
        }
        started = doPrepare(params);
        this.startedRef.set(started);
        return started.option();
    }

    @Override
    public synchronized void close() {
        var started = startedRef.get();
        if (started == null) {
            return;
        }
        started.close();
        startedRef.set(null);
    }

    private StartedInstance doPrepare(Params params) {
        var workDir = resolveWorkDir();
        var dataDir = resolveDataDir();
        var project = params.getProject();

        final EmbeddedPostgres postgres;
        try (var classLoader = GradleUtils.toClassLoader(
                project.getConfigurations().getByName(RUNTIME_NAME_PG)
        )) {
            postgres = EmbeddedPostgres.builder()
                    .setOverrideWorkingDirectory(workDir)
                    .setDataDirectory(dataDir)
                    .setPgBinaryResolver(
                            PgBinaryResolverImpl.create(classLoader)
                    )
                    .start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var url = String.format("jdbc:postgresql://localhost:%s/", postgres.getPort());
        var option = JdbcOptionImpl.builder()
                .url(url)
                .user(POSTGRES)
                .password(POSTGRES)
                .build();

        return new StartedInstance(option, postgres);
    }


    public interface Params {

        @Inject
        Project getProject();
    }

    private record StartedInstance(
            JdbcOption option,
            EmbeddedPostgres db
    ) {
        void close() {
            try {
                db.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @RequiredArgsConstructor(staticName = "create")
    public static class PgBinaryResolverImpl implements PgBinaryResolver {

        private final ClassLoader classLoader;

        @Override
        public InputStream getPgBinary(String system, String machineHardware) throws IOException {
            system = normalize(system);
            var arch = normalize(ArchUtils.normalize(machineHardware));
            var dist = normalize(LinuxUtils.getDistributionName());

            if (dist != null) {
                var resource = findResource(format("postgres-%s-%s-%s.txz", system, arch, dist));
                if (resource != null) {
                    log.info("Distribution specific postgres binaries found: {}", resource.getFilename());
                    return resource.openInputStream();
                }
            }

            var resource = findResource(format("postgres-%s-%s.txz", system, arch));
            if (resource != null) {
                log.info("System specific postgres binaries found: {}", resource.getFilename());
                return resource.openInputStream();
            }
            throw new IllegalStateException("Missing embedded postgres binaries");
        }

        @Nullable
        private Resource findResource(String path) throws IOException {
            var urls = Lists.collect(classLoader.getResources(path));
            if (urls.size() == 1) {
                return new Resource(urls.get(0));
            }
            if (urls.size() > 1) {
                throw new IllegalStateException("Duplicate embedded postgres binaries: " + path);
            }
            return null;
        }

        @Nullable
        private static String normalize(@Nullable String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return lowerCase(input.replace(' ', '_'));
        }

        @RequiredArgsConstructor
        private static class Resource {

            private final URL url;

            public String getFilename() {
                return FilenameUtils.getName(url.getPath());
            }

            public InputStream openInputStream() throws IOException {
                var con = this.url.openConnection();
                try {
                    return con.getInputStream();
                } catch (IOException ex) {
                    // Close the HTTP connection (if applicable).
                    if (con instanceof HttpURLConnection) {
                        ((HttpURLConnection) con).disconnect();
                    }
                    throw ex;
                }
            }
        }
    }

}
