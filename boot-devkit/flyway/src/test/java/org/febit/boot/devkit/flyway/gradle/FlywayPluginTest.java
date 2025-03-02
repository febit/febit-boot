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

import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.febit.boot.devkit.flyway.gradle.FlywayExtension.TASK_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

class FlywayPluginTest {

    @Test
    void tasks() throws IOException {
        var projectDir = new File("./build/test-scene-flyway");
        FileUtils.copyDirectory(new File("./src/test/scene-flyway"), projectDir);

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("flyway-plugin-test")
                .build();
        var tasks = project.getTasks();
        var evaluated = new AtomicBoolean(false);

        project.afterEvaluate(p -> evaluated.set(true));
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(FlywayPlugin.class);

        var extension = project.getExtensions().getByType(FlywayExtension.class);
        extension.getApplicationPropsDir().set(project.getLayout().getProjectDirectory());
        extension.excludeTask(".*-excluded");

        assertFalse(evaluated.get());
        project.evaluate();
        assertTrue(evaluated.get());

        checkTaskNames(tasks.getNames());
        testExecutionMix(project);
    }

    private void checkTaskNames(Set<String> names) {
        for (var action : FlywayAction.values()) {
            assertTrue(names.contains(TASK_PREFIX + action.title() + "-mix"));
        }

        assertFalse(names.contains(TASK_PREFIX + "info-excluded"));
        assertFalse(names.contains(TASK_PREFIX + "clean-excluded"));
        assertFalse(names.contains(TASK_PREFIX + "migrate-excluded"));
    }

    private void testExecutionMix(ProjectInternal project) {
        var tasks = project.getTasks();

        var infoTask = (FlywayTask) tasks.getByName(TASK_PREFIX + "info-mix");
        var jdbc = infoTask.getJdbc().get();
        var option = infoTask.getOption().get();

        assertEquals("jdbc:h2:mem:flyway-test", jdbc.url());
        assertEquals("devkit", jdbc.user());
        assertEquals("boot&devkit", jdbc.password());
        assertEquals("gbk", option.encoding());
        assertEquals("flyway_schema_history_v1", option.table());
        assertEquals("flyway_history_schema", option.tablespace());
        assertEquals(List.of(
                "devkit",
                "boot"
        ), option.schemas());

        assertDoesNotThrow(() -> executeTask(infoTask));
        assertEquals(List.of(
                "classpath:db/migration",
                "classpath:db/migration2"
        ), option.migrationsDirs());
    }

    private static void executeTask(FlywayTask task) {
        task.getTaskActions()
                .forEach(action -> action.execute(task));
    }
}
