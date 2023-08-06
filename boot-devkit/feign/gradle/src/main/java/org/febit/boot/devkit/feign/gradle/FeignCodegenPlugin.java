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
package org.febit.boot.devkit.feign.gradle;

import lombok.val;
import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;

public class FeignCodegenPlugin implements Plugin<Project> {

    @Override
    public void apply(Project proj) {
        proj.getPlugins().apply(JavaBasePlugin.class);

        proj.getExtensions()
                .create(Constants.EXT_NAME, FeignCodegenExtension.class, proj);

        proj.getTasks()
                .register(Constants.TASK_GEN_CLIENT, FeignCodegenExtensionTask.class);

        proj.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project proj) {
        val extension = proj.getExtensions()
                .getByType(FeignCodegenExtension.class);

        val targetDir = extension.getTargetSourceDir();
        FolderUtils.mkdirs(targetDir);
        GradleUtils.mainSourceSet(proj).getJava().srcDir(targetDir);

        val tasks = proj.getTasks();

        // Depend on lombok plugin
        tasks.named(Constants.TASK_GEN_LOMBOK_CONFIG, task -> {
            task.dependsOn(Constants.TASK_GEN_CLIENT);
        });

        tasks.named(Constants.TASK_GEN_CLIENT, task -> {
            val sourceProjectCompileTask = extension.getSourceProject().getTasks()
                    .getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
            task.dependsOn(sourceProjectCompileTask);
        });
    }

}
