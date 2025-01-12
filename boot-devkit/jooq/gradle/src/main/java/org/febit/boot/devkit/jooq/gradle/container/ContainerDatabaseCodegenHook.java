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
package org.febit.boot.devkit.jooq.gradle.container;

import org.febit.boot.devkit.jooq.gradle.ICodegenHook;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenExtension;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenPlugin;
import org.febit.boot.devkit.jooq.gradle.JooqCodegenPrepareTask;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class ContainerDatabaseCodegenHook implements ICodegenHook {

    private static final String WORK_DIR = "codegen-docker-db";
    private static final String USER = "codegen";
    private static final String PASSWORD = "codegen";

    @Override
    public void afterEvaluate(Project project) {
        var extension = project.getExtensions()
                .getByType(JooqCodegenExtension.class);
        var conf = extension.getContainer();
        Objects.requireNonNull(conf.getType(), "Docker database type is required");

        var deps = project.getDependencies();
        deps.add(JooqCodegenPlugin.RUNTIME_NAME, conf.getType().getDriverArtifact());
    }

    private File resolveWorkDir(Project project, JooqCodegenExtension extension) {
        var conf = extension.getContainer();
        var workDir = conf.getWorkingDir();
        if (workDir != null) {
            return workDir;
        }
        var buildDir = project.getLayout()
                .getBuildDirectory()
                .getAsFile()
                .get();
        return new File(buildDir, WORK_DIR);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void beforePrepareTask(JooqCodegenPrepareTask task) {
        var extension = task.codegenExtension();
        var conf = extension.getContainer();
        var project = task.getProject();

        var workDir = resolveWorkDir(project, extension);

        var db = ContainerDatabase.builder()
                .type(conf.getType())
                .image(conf.getImage())
                .dockerBinPath(conf.getDockerBinPath())
                .workingDir(workDir)
                .database(extension.getGenerator()
                        .getDatabase()
                        .getInputSchema()
                )
                .user(USER)
                .password(PASSWORD)
                .build();

        try {
            db.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot start database with container", e);
        }
        project.getGradle().buildFinished(result -> {
            db.close();
        });

        extension.getJooqConfig().getJdbc()
                .withUrl(db.getJdbcUrl())
                .withUser(USER)
                .withPassword(PASSWORD)
        ;
    }

}
