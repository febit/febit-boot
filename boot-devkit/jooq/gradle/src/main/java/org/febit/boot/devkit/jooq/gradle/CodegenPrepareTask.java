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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.devkit.flyway.gradle.FlywayActions;
import org.febit.boot.devkit.flyway.gradle.FlywayTask;
import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class CodegenPrepareTask extends FlywayTask {

    private final Configuration runtime;

    @Inject
    public CodegenPrepareTask(
            Configuration runtime
    ) {
        super(FlywayActions.migrate());
        this.runtime = runtime;

        setGroup(MetaUtils.GROUP_NAME);
        setDescription("Prepare database for codegen");
    }

    public JooqCodegenExtension codegenExtension() {
        return getProject().getExtensions()
                .getByType(JooqCodegenExtension.class);
    }

    private void fillConfig() {
        var extension = codegenExtension();
        var jdbc = extension.getJooqConfig().getJdbc();
        var schema = extension.getJooqConfig().getGenerator()
                .getDatabase().getInputSchema();

        setMigrationsDirs(extension.getMigrationsDirs());
        setExtraClasspath(runtime);
        setUrl(jdbc.getUrl());
        setUser(jdbc.getUser());
        setPassword(jdbc.getPassword());
        if (StringUtils.isNotEmpty(schema)) {
            setSchemas(List.of(schema));
        }
    }

    @Override
    @TaskAction
    public void runTask() {
        var extension = codegenExtension();
        extension.getHook().beforePrepareTask(this);
        fillConfig();
        super.runTask();
    }
}
