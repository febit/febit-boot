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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class FlywayExtension {

    static final String EXTENSION_NAME = "febitFlyway";
    static final String GROUP = "Febit Flyway";
    static final String TASK_PREFIX = "febit-flyway-";
    private static final String DEFAULT_PROPS_DIR = "src/main/resources";

    /**
     * Location to scan spring-boot <code>application-*.{yml|yaml|properties}</code>.
     * (default: src/main/resources")
     */
    @Getter
    @Setter
    @Nonnull
    private String applicationPropsDir = DEFAULT_PROPS_DIR;

    @Getter
    private final List<Pattern> excludeTasks = new ArrayList<>();

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see FluentConfiguration#locations(String...)
     */
    @Getter
    private final List<String> migrationsDirs = new ArrayList<>();

    public void excludeTask(Pattern exclude) {
        excludeTasks.add(exclude);
    }

    public void excludeTask(String exclude) {
        excludeTask(Pattern.compile(exclude));
    }

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see #migrationsDir(String...)
     * @see FluentConfiguration#locations(String...)
     */
    public void migrationsDir(String dir) {
        this.migrationsDirs.add(dir);
    }

    /**
     * Location to scan recursively for migrations.
     * (default: db/migration)
     *
     * @see #migrationsDir(String)
     * @see FluentConfiguration#locations(String...)
     */
    public void migrationsDir(String... dirs) {
        this.migrationsDirs.addAll(List.of(dirs));
    }

    boolean isTaskExcluded(String name) {
        for (Pattern exclude : excludeTasks) {
            if (exclude.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

}
