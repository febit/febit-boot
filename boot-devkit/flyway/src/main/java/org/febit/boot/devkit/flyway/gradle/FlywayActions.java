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

import lombok.experimental.UtilityClass;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

import java.util.function.Consumer;

import static org.febit.devkit.gradle.util.GradleUtils.println;

@UtilityClass
public class FlywayActions {

    public static Consumer<Flyway> info() {
        return FlywayActions::info;
    }

    public static Consumer<Flyway> undo() {
        return Flyway::undo;
    }

    public static Consumer<Flyway> clean() {
        return Flyway::clean;
    }

    public static Consumer<Flyway> repair() {
        return Flyway::repair;
    }

    public static Consumer<Flyway> migrate() {
        return Flyway::migrate;
    }

    public static Consumer<Flyway> baseline() {
        return Flyway::baseline;
    }

    public static Consumer<Flyway> validate() {
        return Flyway::validate;
    }

    private static void info(Flyway flyway) {
        var info = flyway.info();
        var current = info.current();
        var version = current == null
                ? MigrationVersion.EMPTY
                : current.getVersion();
        println("Migration version: " + version);
        println(MigrationInfoDumper.dumpToAsciiTable(info.all()));
    }
}
