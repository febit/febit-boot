package org.febit.devkit.gradle.codegen.jooq;

import org.febit.jooq.codegen.meta.MetaUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.JavaExecSpec;
import org.jooq.meta.jaxb.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

public class CodegenJooqTask extends DefaultTask {

    private final FileCollection classpath;
    private final Configuration conf;

    @Inject
    public CodegenJooqTask(FileCollection classpath, Configuration conf) {
        setGroup(MetaUtils.GROUP_NAME);
        setDescription("Generates the jOOQ sources");
        this.classpath = classpath;
        this.conf = conf;
    }

    @TaskAction
    public void generate() {
        getProject().javaexec(this::configSpec);
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
