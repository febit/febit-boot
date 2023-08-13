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

import org.febit.devkit.gradle.util.GradleUtils;
import org.flywaydb.core.Flyway;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;

import java.io.File;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.*;
import static org.febit.boot.devkit.flyway.gradle.FlywayExtension.TASK_PREFIX;

public class FlywayPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(JavaBasePlugin.class);
        project.getExtensions().create(
                FlywayExtension.EXTENSION_NAME, FlywayExtension.class
        );
        project.afterEvaluate(p -> afterEvaluate());
    }

    private void afterEvaluate() {
        var extension = project.getExtensions()
                .getByType(FlywayExtension.class);

        var baseDir = new File(project.getProjectDir(), extension.getApplicationPropsDir());

        GradleUtils.println("Scan profiles from dir " + baseDir);
        var files = baseDir.listFiles(f ->
                f.getName().matches("application-.*\\.(yml|yaml|properties)")
        );

        if (files == null) {
            return;
        }

        for (var f : files) {
            var profile = removeStart(
                    substringBeforeLast(f.getName(), "."),
                    "application-"
            );
            addTask(profile, f, "info", FlywayActions.info());
            addTask(profile, f, "undo", FlywayActions.undo());
            addTask(profile, f, "clean", FlywayActions.clean());
            addTask(profile, f, "repair", FlywayActions.repair());
            addTask(profile, f, "migrate", FlywayActions.migrate());
            addTask(profile, f, "baseline", FlywayActions.baseline());
            addTask(profile, f, "validate", FlywayActions.validate());
        }
    }

    private void addTask(String profile, File profileFile, String actionName, Consumer<Flyway> action) {
        var extension = project.getExtensions().getByType(FlywayExtension.class);
        var name = TASK_PREFIX + actionName + "-" + profile;
        if (extension.isTaskExcluded(name)) {
            return;
        }

        var task = project.getTasks()
                .create(name, FlywayTask.class, action);
        task.setMigrationsDirs(extension.getMigrationsDirs());
        task.setDescription("Flyway " + actionName + ", profile: " + profile);
        task.doFirst(t -> beforeRun(task, profileFile));
    }

    private void beforeRun(FlywayTask task, File profile) {
        var ext = substringAfterLast(profile.getName(), ".");
        var sources = new String[]{
                new File(profile.getParentFile(), "application." + ext).getAbsolutePath(),
                profile.getAbsolutePath()
        };
        var props = switch (ext) {
            case "properties" -> SpringPropsLoader.properties(sources);
            case "yml", "yaml" -> SpringPropsLoader.yaml(sources);
            default -> throw new IllegalArgumentException("Unsupported profile format '"
                    + ext + "': " + profile.getName());
        };

        task.setUrl(props.getProperty("spring.datasource.url"));
        task.setUser(props.getProperty("spring.datasource.username"));
        task.setPassword(props.getProperty("spring.datasource.password"));

        task.setEncoding(props.getProperty("spring.flyway.encoding"));
        task.setTable(props.getProperty("spring.flyway.table"));
        task.setTablespace(props.getProperty("spring.flyway.tablespace"));

        var schemas = SpringPropsLoader.resolveList(props, "spring.flyway.schemas", 100);
        if (!schemas.isEmpty()) {
            task.setSchemas(schemas);
        }

        var locations = SpringPropsLoader.resolveList(props, "spring.flyway.locations", 100);
        if (!locations.isEmpty()) {
            task.setMigrationsDirs(locations);
        }
    }

}
