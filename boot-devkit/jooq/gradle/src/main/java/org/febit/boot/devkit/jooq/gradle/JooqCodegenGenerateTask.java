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

import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.JavaExecSpec;
import org.jooq.meta.jaxb.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

public class JooqCodegenGenerateTask extends DefaultTask {

    private final FileCollection classpath;
    private final Configuration conf;
    private final ExecOperations exec;

    @Inject
    public JooqCodegenGenerateTask(
            FileCollection classpath,
            ExecOperations exec,
            Configuration conf
    ) {
        this.exec = exec;
        setGroup(MetaUtils.GROUP_NAME);
        setDescription("Generates the jOOQ sources");
        this.classpath = classpath;
        this.conf = conf;
    }

    @TaskAction
    public void generate() {
        exec.javaexec(this::configSpec);
    }

    private void configSpec(JavaExecSpec spec) {

        var confFile = new File(getTemporaryDir(), "config.xml");
        MetaUtils.emitConfigSilent(conf, confFile);

        spec.setClasspath(classpath);
        spec.setWorkingDir(getProject().getProjectDir());
        spec.getMainClass().set(MetaUtils.CLASS_MAIN);
        spec.setArgs(List.of(confFile));
    }

}
