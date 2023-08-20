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
package org.febit.boot.devkit.flyway.gradle;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.lang.util.Lists;
import org.febit.lang.util.Sets;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.febit.devkit.gradle.util.GradleUtils.println;

@Slf4j
@Getter
@Setter
public class FlywayTask extends DefaultTask {

    @Internal
    @Nonnull
    private final Consumer<Flyway> flywayAction;

    @Optional
    @InputFiles
    @Nullable
    private FileCollection extraClasspath;

    @Optional
    @Input
    @Nonnull
    private String url;

    @Optional
    @Input
    @Nullable
    private String user;

    @Optional
    @Input
    @Nullable
    private String password;

    /**
     * The encoding of Sql migrations.
     *
     * @see FluentConfiguration#encoding(String)
     */
    @Optional
    @Input
    @Nullable
    private String encoding;

    /**
     * Schema history table.
     *
     * @see FluentConfiguration#table(String)
     */
    @Optional
    @Input
    @Nullable
    private String table;

    /**
     * Where to create the schema history table.
     *
     * @see FluentConfiguration#tablespace(String)
     */
    @Optional
    @Input
    @Nullable
    private String tablespace;

    @Optional
    @Input
    @Nullable
    private List<String> schemas;

    /**
     * Locations to scan recursively for migrations.
     *
     * @see FluentConfiguration#locations(String...)
     */
    @Optional
    @Input
    @Nullable
    private List<String> migrationsDirs;

    @Inject
    public FlywayTask(Consumer<Flyway> action) {
        Objects.requireNonNull(action);

        setGroup(FlywayExtension.GROUP);
        this.flywayAction = action;
    }

    @TaskAction
    public void runTask(InputChanges inputChanges) {
        try {
            var conf = createFlywayConf();
            printConf(conf);

            var flyway = conf.load();
            var ds = (DriverDataSource) flyway.getConfiguration().getDataSource();

            flywayAction.accept(flyway);
            ds.shutdownDatabase();
        } catch (Exception e) {
            throw new FlywayException("Error occurred while executing task '" + getName() + "'", e);
        }
    }

    protected FluentConfiguration createFlywayConf() {
        var classLoader = new URLClassLoader(
                extraClasspathUrls(),
                getProject().getBuildscript().getClassLoader()
        );

        var conf = Flyway.configure(classLoader)
                .dataSource(this.url, this.user, this.password);

        if (this.encoding != null) {
            conf.encoding(this.encoding);
        }
        if (this.table != null) {
            conf.table(this.table);
        }
        if (this.tablespace != null) {
            conf.tablespace(this.tablespace);
        }
        if (CollectionUtils.isNotEmpty(this.schemas)) {
            conf.schemas(this.schemas.toArray(new String[0]));
        }
        if (CollectionUtils.isNotEmpty(this.migrationsDirs)) {
            conf.locations(this.migrationsDirs.toArray(new String[0]));
        }
        return conf;
    }

    protected void printConf(FluentConfiguration conf) {
        println("Flyway Configuration");
        println("         url: " + conf.getUrl());
        println("        user: " + conf.getUser());
        println("    password: " + StringUtils.isNotEmpty(conf.getPassword()));
        println("       table: " + conf.getTable());
        Stream.of(conf.getSchemas()).forEach(schema ->
                println("     schemas: " + schema)
        );
        Stream.of(conf.getLocations()).forEach(location ->
                println("  migrations: " + location)
        );
    }

    protected URL[] extraClasspathUrls() {
        var uris = Sets.collect(
                extraClasspath != null
                        ? extraClasspath.getFiles() : null,
                File::toURI
        );

        var main = GradleUtils.mainSourceSet(getProject());

        // Main resources
        uris.addAll(Lists.collect(
                main.getResources().getSrcDirs(),
                File::toURI
        ));

        // Main classpath
        uris.addAll(Lists.collect(
                main.getRuntimeClasspath(),
                File::toURI
        ));

        return uris.stream()
                .map(this::toUrl)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
    }

    @Nullable
    private URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            log.warn("Cannot resolve URL for flyway classpath：{}", uri);
            return null;
        }
    }

}
