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
import org.febit.boot.devkit.flyway.gradle.FlywayAction;
import org.febit.boot.devkit.flyway.gradle.FlywayExecutor;
import org.febit.boot.devkit.flyway.gradle.model.FlywayOptionImpl;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.flywaydb.core.api.FlywayException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.JavaExecSpec;
import org.jooq.meta.jaxb.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
@CacheableTask
public abstract class JooqCodegenGenerateTask extends DefaultTask {

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Inject
    public abstract ScriptHandler getScriptHandler();

    @Input
    protected abstract Property<JdbcProvider<?>> getJdbcProvider();

    @Classpath
    protected abstract Property<FileCollection> getClasspath();

    @Classpath
    protected abstract Property<FileCollection> getMigrationsClasspath();

    @Input
    protected abstract Property<Configuration> getConf();

    @Inject
    protected abstract ExecOperations getExec();

    @Input
    protected abstract ListProperty<String> getMigrationsDirs();

    @Optional
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    protected abstract Property<FileCollection> getInputDirs();

    @Internal
    protected abstract DirectoryProperty getWorkDir();

    @OutputDirectory
    protected abstract DirectoryProperty getGeneratedSourceDir();

    @TaskAction
    public void generate() {
        var objects = getObjectFactory();
        try (var conn = getJdbcProvider().get().open(objects)) {
            var jdbc = conn.option();

            applyMigrations(jdbc);

            getExec().javaexec(spec -> {
                configSpec(spec, jdbc);
            });
        }
    }

    private void applyMigrations(JdbcOption jdbc) {
        var classloader = getScriptHandler().getClassLoader();
        var conf = getConf().get();
        var schema = conf.getGenerator()
                .getDatabase()
                .getInputSchema();

        var flywayOption = FlywayOptionImpl.builder()
                .migrationsDirs(getMigrationsDirs().get())
                .schemas(StringUtils.isEmpty(schema) ? List.of() : List.of(schema))
                .build();

        var extraClasspath = getMigrationsClasspath().get().plus(
                getClasspath().get()
        );

        try {
            FlywayExecutor.builder()
                    .action(FlywayAction.MIGRATE)
                    .jdbc(jdbc)
                    .option(flywayOption)
                    .extraClasspath(extraClasspath)
                    .baseClassLoader(classloader)
                    .exec();
        } catch (
                Exception e) {
            throw new FlywayException("Error occurred while executing task '" + getName() + "'", e);
        }
    }

    private void configSpec(JavaExecSpec spec, JdbcOption jdbc) {
        var sourceDir = getGeneratedSourceDir().get().getAsFile().getAbsolutePath();
        var classpath = getClasspath().get();
        var workDir = getWorkDir().get().getAsFile();

        var inputDirs = getInputDirs().getOrNull();
        if (inputDirs != null) {
            log.debug("Input directories:");
            inputDirs.forEach(dir -> {
                log.debug(" - {}", dir);
            });
        }

        var conf = getConf().get();
        conf.getGenerator().getTarget()
                .setDirectory(sourceDir);
        conf.getJdbc()
                .withUrl(jdbc.url())
                .withUser(jdbc.user())
                .withPassword(jdbc.password());

        var confFile = new File(getTemporaryDir(), "config.xml");
        MetaUtils.emitConfigSilent(conf, confFile);

        spec.setClasspath(classpath);
        spec.setWorkingDir(workDir);
        spec.getMainClass().set(MetaUtils.CLASS_MAIN);
        spec.setArgs(List.of(confFile));
    }

}
