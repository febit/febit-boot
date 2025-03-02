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

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.devkit.flyway.gradle.model.FlywayOption;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.lang.util.Sets;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;

import static org.febit.devkit.gradle.util.GradleUtils.println;

@Slf4j
@UtilityClass
public class FlywayExecutor {

    @lombok.Builder(
            builderClassName = "Builder",
            buildMethodName = "exec"
    )
    public static void exec(
            @lombok.NonNull
            FlywayAction action,
            @lombok.NonNull
            ClassLoader baseClassLoader,
            @lombok.NonNull
            JdbcOption jdbc,
            @lombok.NonNull
            FlywayOption option,

            @Nullable Boolean printConf,
            @Nullable FileCollection extraClasspath
    ) {
        if (printConf == null) {
            printConf = false;
        }

        var confBuilder = configurationBuilder()
                .baseClassLoader(baseClassLoader)
                .jdbc(jdbc)
                .option(option);
        if (extraClasspath != null) {
            confBuilder.extraClasspath(extraClasspath);
        }

        var conf = confBuilder.build();
        if (Boolean.TRUE.equals(printConf)) {
            printConf(conf);
        }

        var flyway = conf.load();
        var ds = (DriverDataSource) flyway.getConfiguration().getDataSource();

        action.apply(flyway);
        ds.shutdownDatabase();
    }

    @lombok.Builder(
            builderClassName = "ConfigurationBuilder",
            builderMethodName = "configurationBuilder"
    )
    private static FluentConfiguration createFlywayConf(
            @lombok.NonNull
            ClassLoader baseClassLoader,
            @lombok.NonNull
            JdbcOption jdbc,
            @lombok.NonNull
            FlywayOption option,

            @Nullable FileCollection extraClasspath
    ) {
        var classLoader = new URLClassLoader(
                extraClasspathUrls(extraClasspath), baseClassLoader
        );

        var conf = Flyway.configure(classLoader)
                .dataSource(jdbc.url(), jdbc.user(), jdbc.password());

        if (option.encoding() != null) {
            conf.encoding(option.encoding());
        }
        if (option.table() != null) {
            conf.table(option.table());
        }
        if (option.tablespace() != null) {
            conf.tablespace(option.tablespace());
        }
        var schemas = option.schemas();
        if (CollectionUtils.isNotEmpty(schemas)) {
            conf.schemas(schemas.toArray(new String[0]));
        }
        var locations = option.migrationsDirs();
        if (CollectionUtils.isNotEmpty(locations)) {
            conf.locations(locations.toArray(new String[0]));
        }
        return conf;
    }

    public static void printConf(FluentConfiguration conf) {
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

    private static URL[] extraClasspathUrls(@Nullable FileCollection extraClasspath) {
        var uris = Sets.collect(
                extraClasspath != null
                        ? extraClasspath.getFiles() : null,
                File::toURI
        );

        return uris.stream()
                .map(FlywayExecutor::toUrl)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
    }

    @Nullable
    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            log.warn("Cannot resolve URL for flyway classpath：{}", uri);
            return null;
        }
    }
}
