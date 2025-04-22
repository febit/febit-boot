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

import lombok.extern.slf4j.Slf4j;
import org.febit.boot.devkit.flyway.gradle.model.FlywayOptionImpl;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOptionImpl;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Slf4j
public class FlywayPlugin implements Plugin<Project> {

    static final String EXTENSION = "febitFlyway";
    static final String RUNTIME = EXTENSION;
    static final String GROUP = "Febit Flyway";
    static final String TASK_PREFIX = "febit-flyway-";

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(JavaBasePlugin.class);
        project.getExtensions().create(
                EXTENSION, FlywayExtension.class
        );
        project.afterEvaluate(p -> afterEvaluate());
        project.getConfigurations()
                .create(RUNTIME)
                .setDescription("Classpath for Flyway, add your JDBC drivers or extension libs here.");
    }

    private void afterEvaluate() {
        var extension = project.getExtensions()
                .getByType(FlywayExtension.class);

        var baseDir = extension.getApplicationPropsDir().get().getAsFile();
        log.info("Scan profiles in dir: {}", baseDir.getAbsolutePath());
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

            for (var action : FlywayAction.values()) {
                addTask(profile, f, action);
            }
        }
    }

    private void addTask(String profile, File profileFile, FlywayAction action) {
        var extension = project.getExtensions().getByType(FlywayExtension.class);
        var name = TASK_PREFIX + action.title() + "-" + profile;
        if (extension.isTaskExcluded(name)) {
            return;
        }

        project.getTasks().register(name, FlywayTask.class,
                task -> config(task, action, profile, profileFile)
        );
    }

    private void config(FlywayTask task, FlywayAction action, String profile, File profileFile) {
        var extension = project.getExtensions().getByType(FlywayExtension.class);

        task.setDescription("Flyway " + action.title() + ", profile: " + profile);
        task.getAction().convention(action);

        var main = GradleUtils.mainSourceSet(project);

        var extraClasspath = project.getConfigurations().getByName(RUNTIME)
                .plus(main.getResources().getSourceDirectories());
        task.getExtraClasspath().convention(extraClasspath);

        var propsRef = project.getObjects()
                .property(File.class)
                .convention(profileFile)
                .map(Props::of);

        task.getJdbc().convention(project.provider(() -> {
            var props = propsRef.get();
            return JdbcOptionImpl.builder()
                    .url(props.property(
                            "spring.flyway.url",
                            "spring.datasource.url"
                    ))
                    .user(props.property(
                            "spring.flyway.username",
                            "spring.datasource.username"
                    ))
                    .password(props.property(
                            "spring.flyway.password",
                            "spring.datasource.password"
                    ))
                    .build();
        }));

        task.getOption().convention(project.provider(() -> {
            var props = propsRef.get();
            return FlywayOptionImpl.builder()
                    .encoding(props.property("spring.flyway.encoding"))
                    .table(props.property("spring.flyway.table"))
                    .tablespace(props.property("spring.flyway.tablespace"))
                    .schemas(props.listProperty("spring.flyway.schemas", 100))
                    .migrationsDirs(extension.getMigrationsDirs().get())
                    .migrationsDirs(props.listProperty("spring.flyway.locations", 100))
                    .build();
        }));

    }

    private record Props(
            File profile,
            Properties properties
    ) implements Serializable {

        @Nullable
        public String property(String key) {
            return properties.getProperty(key);
        }

        @Nullable
        public String property(String key, String... orKeys) {
            var value = property(key);
            if (value != null) {
                return value;
            }
            for (var k : orKeys) {
                value = property(k);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        public List<String> listProperty(String key, int max) {
            return SpringPropsLoader.resolveList(properties, key, max);
        }

        public static Props of(File profile) {
            return new Props(profile, load(profile));
        }

        private static Properties load(File profile) {
            var ext = substringAfterLast(profile.getName(), ".");
            var sources = new String[]{
                    new File(profile.getParentFile(), "application." + ext).getAbsolutePath(),
                    profile.getAbsolutePath()
            };
            return switch (ext) {
                case "properties" -> SpringPropsLoader.properties(sources);
                case "yml", "yaml" -> SpringPropsLoader.yaml(sources);
                default -> throw new IllegalArgumentException("Unsupported profile format '"
                        + ext + "': " + profile.getName());
            };
        }
    }
}
