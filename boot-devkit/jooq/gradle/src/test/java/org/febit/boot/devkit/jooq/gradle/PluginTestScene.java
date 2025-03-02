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
package org.febit.boot.devkit.jooq.gradle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.febit.boot.devkit.jooq.gradle.container.ContainerDatabaseJdbcProvider;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.lang.util.ArraysUtils;
import org.gradle.api.JavaVersion;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskContainerInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.jooq.meta.jaxb.Jdbc;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.boot.devkit.jooq.gradle.JooqCodegenExtension.DIR_GENERATED_SRC;
import static org.febit.boot.devkit.jooq.gradle.JooqCodegenPlugin.TASK_NAME_GENERATE_JOOQ;
import static org.junit.jupiter.api.Assertions.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginTestScene {

    static final String SCHEMA = "scene_test";

    static final String PKG = "org.febit.jooq.test";
    static final String TARGET_DIR = "build/" + DIR_GENERATED_SRC
            + '/' + StringUtils.replaceChars(PKG, '.', '/');

    static final Map<Object, Object> SCHEMA_FOO = Map.of(
            "status", "enum:" + PKG + ".model.FooStatus",
            "json", "json:" + PKG + ".model.JsonBean",
            "json_varchar", "json:" + PKG + ".model.JsonBean",
            "json_text", "json:" + PKG + ".model.JsonBean",
            "string_json_bean", "json:" + PKG + ".model.JsonBean",
            "string_json_bean_array", "json:array:" + PKG + ".model.JsonBean",
            "string_json_bean_list", "json:list:" + PKG + ".model.JsonBean",
            "string_json_bean_map", "json:map<int," + PKG + ".model.JsonBean>",
            "string_json_map", "json:map<int,boolean>"
    );

    private final File projectDir;
    private final ProjectInternal project;

    private final ForcedTypesHandler forcedTypes = new ForcedTypesHandler();

    static void executeTask(TaskInternal task) {
        task.getTaskActions()
                .forEach(action -> action.execute(task));
    }

    public boolean isFileExists(String path) {
        return new File(projectDir, path).exists();
    }

    public File file(String path) {
        return new File(projectDir, path);
    }

    public TaskContainerInternal tasks() {
        return project.getTasks();
    }

    public JooqCodegenExtension extension() {
        return project.getExtensions().getByType(JooqCodegenExtension.class);
    }

    public void presetJdbc(Consumer<Jdbc> customizer) {
        var jdbc = extension().getJdbc();
        extension().getJdbcProvider()
                .convention(new PresetJdbcProvider(jdbc));
        customizer.accept(jdbc.get());
    }

    public void containerJdbc(Consumer<ContainerDbConfig> customizer) {
        var extension = extension();
        var container = extension.getContainer().get();
        customizer.accept(container);
        extension.getJdbcProvider().convention(ContainerDatabaseJdbcProvider.builder()
                .conf(container)
                .generator(extension.getGenerator().get())
                .buildDir(project.getLayout().getBuildDirectory().getAsFile().get())
                .build());
    }

    public void embeddedPostgres(Consumer<EmbeddedPostgresConfig> customizer) {
        extension().embeddedPostgres();
        customizer.accept(extension().getEmbeddedPostgres());
    }

    public void execute() {
        extension().getGenerator().get()
                .getDatabase()
                .getForcedTypes()
                .addAll(forcedTypes);

        project.evaluate();
        var codegenTask = assertDoesNotThrow(() ->
                (JooqCodegenGenerateTask) tasks().getByName(TASK_NAME_GENERATE_JOOQ)
        );

        assertTrue(isFileExists("src/codegen-jooq/java"));
        assertTrue(isFileExists("src/codegen-jooq/resources"));
        assertTrue(isFileExists("src/main/resources/db/migration"));

        executeTask(codegenTask);

        var srcDirs = GradleUtils.mainSourceSet(project).getAllJava().getSrcDirs();
        assertThat(srcDirs)
                .contains(extension().getGeneratedSourceDir().get().getAsFile());

        assertTrue(isFileExists(TARGET_DIR + "/DefaultCatalog.java"));
        assertTrue(isFileExists(TARGET_DIR + "/SceneTest.java"));
        assertTrue(isFileExists(TARGET_DIR + "/Indexes.java"));
        assertTrue(isFileExists(TARGET_DIR + "/Keys.java"));
        assertTrue(isFileExists(TARGET_DIR + "/Tables.java"));
    }

    public static PluginTestScene create(
            @lombok.NonNull
            String template,
            @lombok.NonNull
            String name
    ) throws IOException {
        JooqCodegenPlugin.INTERNAL_TESTING_MODE.set(true);

        var projectDir = new File("./build/test-plugin-scenes/" + name);
        FileUtils.copyDirectory(new File("./src/test/scene-" + template), projectDir);

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("jooq-codegen-plugin-test-" + name)
                .build();

        initPlugin(project);

        var extension = project.getExtensions().getByType(JooqCodegenExtension.class);
        extension.setSchema(SCHEMA);
        extension.setTargetPackage(PKG);

        return new PluginTestScene(projectDir, project);
    }

    private static void initPlugin(ProjectInternal project) {
        var repos = project.getRepositories();

        // TODO remote repos
        repos.mavenCentral();

        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JooqCodegenPlugin.class);

        var javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaPluginExtension.setSourceCompatibility(JavaVersion.VERSION_17);
        javaPluginExtension.setTargetCompatibility(JavaVersion.VERSION_17);

        var springBootVersion = System.getProperty("APP_SPRING_BOOT_VERSION");
        if (StringUtils.isEmpty(springBootVersion)) {
            throw new IllegalStateException("Please set system property: APP_SPRING_BOOT_VERSION for plugin testing");
        }

        var deps = project.getDependencies();

        deps.add(JooqCodegenPlugin.RUNTIME_NAME, deps.enforcedPlatform(
                "org.springframework.boot:spring-boot-dependencies:" + springBootVersion
        ));

        deps.add(JooqCodegenPlugin.RUNTIME_NAME, "org.slf4j:slf4j-simple");
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, "com.h2database:h2");
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, "org.postgresql:postgresql");
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, "com.mysql:mysql-connector-j");

        // Note: add classpath of this test
        var runtime = project.files(ArraysUtils.collect(
                StringUtils.split(System.getProperty("java.class.path"), SystemUtils.IS_OS_WINDOWS ? ";" : ",:"),
                Object[]::new,
                File::new
        ));
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, runtime);
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, runtime);
    }
}
