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
package org.febit.boot.devkit.jooq.gradle.container;

import lombok.Getter;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOptionImpl;
import org.febit.boot.devkit.jooq.gradle.ContainerDbConfig;
import org.febit.boot.devkit.jooq.gradle.JdbcProvider;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenExtension;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenPlugin;
import org.gradle.api.Project;
import org.jooq.meta.jaxb.Generator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@lombok.Builder(
        builderClassName = "Builder"
)
public class ContainerDatabaseJdbcProvider
        implements JdbcProvider<ContainerDatabaseJdbcProvider.Params>, Serializable {

    private static final String WORK_DIR = "codegen-docker-db";
    private static final String USER = "codegen";
    private static final String PASSWORD = "codegen";

    private final AtomicReference<JdbcOption> jdbcRef = new AtomicReference<>();
    private final AtomicReference<ContainerDatabase> dbRef = new AtomicReference<>();

    @Getter
    private final ContainerDbConfig conf;
    @Getter
    private final Generator generator;
    @Getter
    private final File buildDir;

    public interface Params {
    }

    @Override
    public void afterEvaluate(Project project) {
        var extension = project.getExtensions()
                .getByType(JooqCodegenExtension.class);
        var conf = extension.getContainer().get();
        Objects.requireNonNull(conf.getType(), "Docker database type is required");

        var deps = project.getDependencies();
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, conf.getType().getDriverArtifact());
    }

    private File resolveWorkDir() {
        var conf = getConf();
        var workDir = conf.getWorkingDir();
        if (workDir != null) {
            return workDir;
        }
        return new File(buildDir, WORK_DIR);
    }

    @Override
    public synchronized JdbcOption prepare(Params params) {
        var jdbc = this.jdbcRef.get();
        if (jdbc != null) {
            return jdbc;
        }
        jdbc = doPrepare(params);
        this.jdbcRef.set(jdbc);
        return jdbc;
    }

    private JdbcOption doPrepare(Params params) {
        var conf = getConf();
        var workDir = resolveWorkDir();

        @SuppressWarnings("DataFlowIssue")
        var db = ContainerDatabase.builder()
                .type(conf.getType())
                .image(conf.getImage())
                .dockerBinPath(conf.getDockerBinPath())
                .workingDir(workDir)
                .database(getGenerator()
                        .getDatabase()
                        .getInputSchema()
                )
                .user(USER)
                .password(PASSWORD)
                .build();

        try {
            db.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot start database with container", e);
        }

        return JdbcOptionImpl.builder()
                .url(db.getJdbcUrl())
                .user(USER)
                .password(PASSWORD)
                .build();
    }

    @Override
    public synchronized void close() {
        var db = dbRef.get();
        if (db == null) {
            return;
        }
        db.close();
        dbRef.set(null);
    }

}
