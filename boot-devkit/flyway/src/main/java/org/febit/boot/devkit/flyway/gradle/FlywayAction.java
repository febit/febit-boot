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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

import java.io.Serializable;

import static org.febit.devkit.gradle.util.GradleUtils.println;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum FlywayAction {

    INFO("info", FlywayAction::info),
    UNDO("undo", Flyway::undo),
    CLEAN("clean", Flyway::clean),
    REPAIR("repair", Flyway::repair),
    MIGRATE("migrate", Flyway::migrate),
    BASELINE("baseline", Flyway::baseline),
    VALIDATE("validate", Flyway::validate),
    ;

    private final String title;
    private final ActionFunction function;

    public void apply(Flyway flyway) {
        function.apply(flyway);
    }

    @FunctionalInterface
    public interface ActionFunction extends Serializable {
        void apply(Flyway flyway);
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
