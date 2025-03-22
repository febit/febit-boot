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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.devkit.flyway.gradle.model.FlywayOption;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.flywaydb.core.api.FlywayException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

@Slf4j
@Getter
@Setter
@CacheableTask
public abstract class FlywayTask extends DefaultTask {

    @Inject
    public abstract ScriptHandler getScriptHandler();

    @Input
    public abstract Property<FlywayAction> getAction();

    @Classpath
    @Optional
    public abstract Property<FileCollection> getExtraClasspath();

    @Input
    public abstract Property<JdbcOption> getJdbc();

    @Input
    public abstract Property<FlywayOption> getOption();

    @Inject
    public FlywayTask() {
        setGroup(FlywayPlugin.GROUP);
    }

    @TaskAction
    public void runTask() {
        try {
            var classloader = getScriptHandler().getClassLoader();
            FlywayExecutor.builder()
                    .action(getAction().get())
                    .jdbc(getJdbc().get())
                    .option(getOption().get())
                    .extraClasspath(getExtraClasspath().get())
                    .baseClassLoader(classloader)
                    .exec();
        } catch (Exception e) {
            throw new FlywayException("Error occurred while executing task '" + getName() + "'", e);
        }
    }

}
