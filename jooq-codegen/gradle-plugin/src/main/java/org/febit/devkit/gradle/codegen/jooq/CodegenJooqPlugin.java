package org.febit.devkit.gradle.codegen.jooq;

import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.jooq.codegen.meta.CodegenJooqModule;
import org.febit.jooq.codegen.meta.MetaUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;

public class CodegenJooqPlugin implements Plugin<Project> {

    public static final String RUNTIME_NAME = MetaUtils.CODEGEN_JOOQ;
    public static final String EXTENSION_NAME = MetaUtils.CODEGEN_JOOQ;
    public static final String TASK_NAME_GEN = MetaUtils.CODEGEN_JOOQ;

    static final String DIR_GENERATED_SRC = "build/generated/sources/" + MetaUtils.CODEGEN_JOOQ_FOLDER;

    private static final String DIR_CODEGEN_SRC_JAVA = "src/" + MetaUtils.CODEGEN_JOOQ_FOLDER + "/java";
    private static final String DIR_CODEGEN_SRC_RESOURCES = "src/" + MetaUtils.CODEGEN_JOOQ_FOLDER + "/resources";

    private static volatile boolean internalTestingMode = false;

    static void setInternalTestingMode(boolean value) {
        internalTestingMode = value;
    }

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JavaBasePlugin.class);

        var extension = project.getExtensions()
                .create(EXTENSION_NAME, CodegenJooqExtension.class, project);

        var runtime = project.getConfigurations()
                .create(RUNTIME_NAME)
                .setDescription("Classpath for jOOQ generator, add your JDBC drivers or extension libs here.");

        var sourceSets = GradleUtils.sourceSets(project);

        var mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        mainSourceSet.getJava().srcDir(extension.getTargetDir());

        var deps = project.getDependencies();

        if (!internalTestingMode) {
            deps.add(RUNTIME_NAME,
                    "org.febit.jooq.codegen:febit-jooq-codegen-runtime:" + CodegenJooqModule.version()
            );
        }

        var sourceSet = createSourceSet(sourceSets, project);
        sourceSet.setCompileClasspath(runtime);

        var codegenTask = project.getTasks()
                .create(TASK_NAME_GEN, CodegenJooqTask.class,
                        runtime.plus(sourceSet.getOutput()),
                        extension.getJooqConfig());

        codegenTask.dependsOn(
                sourceSet.getCompileJavaTaskName(),
                sourceSet.getProcessResourcesTaskName()
        );
        project.getTasks()
                .getByName(mainSourceSet.getCompileJavaTaskName())
                .dependsOn(codegenTask.getName());

        project.afterEvaluate(proj ->
                proj.getTasks().named(sourceSet.getProcessResourcesTaskName(), Copy.class,
                        task -> task.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE)
                )
        );
    }

    private SourceSet createSourceSet(SourceSetContainer sourceSets, Project project) {
        var sourceSet = sourceSets.create(MetaUtils.CODEGEN_JOOQ);
        var javaSrc = new File(project.getProjectDir(), DIR_CODEGEN_SRC_JAVA);
        var resourcesSrc = new File(project.getProjectDir(), DIR_CODEGEN_SRC_RESOURCES);

        FolderUtils.mkdirs(javaSrc);
        FolderUtils.mkdirs(resourcesSrc);
        sourceSet.getJava().srcDir(javaSrc);
        sourceSet.getResources().srcDir(resourcesSrc);
        return sourceSet;
    }

}
