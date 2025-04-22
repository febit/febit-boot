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

import lombok.extern.slf4j.Slf4j;
import org.febit.boot.devkit.feign.ClientCodegen;
import org.febit.boot.devkit.feign.MetaResolver;
import org.febit.boot.devkit.feign.util.ClassNamings;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.lang.annotation.NonNullApi;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.classpath.DefaultClassPath;

import javax.inject.Inject;
import java.net.URLClassLoader;

@Slf4j
@NonNullApi
public class FeignCodegenExtensionTask extends DefaultTask {

    @Inject
    public FeignCodegenExtensionTask() {
        setGroup(Constants.GROUP_NAME);
        setDescription("Generate api definitions as feign clients.");
    }

    private void generate(
            FeignCodegenExtension extension,
            ClassLoader classloader
    ) {
        var metaResolver = MetaResolver.create(classloader);

        var beans = metaResolver.scanBeans(extension.getScanPackages());
        var clients = metaResolver.resolveClients(beans.values());

        log.info("Found [{}] clients", clients.size());

        var codegen = ClientCodegen.builder()
                .beans(beans)
                .clients(clients)
                .clientName(extension.getClient().getName())
                .clientUrl(extension.getClient().getUrl())
                .clientBasePackage(extension.getClient().getBasePackage())
                .clientConfigClasses(extension.getClient().getConfigClasses())
                .clientNaming(ClassNamings.chain(
                        extension.getNaming().getGlobal(),
                        extension.getNaming().getClient()
                ))
                .pojoNaming(ClassNamings.chain(
                        extension.getNaming().getGlobal(),
                        extension.getNaming().getPojo()
                ))
                .excludedClasses(extension.getExcludedClasses())
                .targetDir(extension.getTargetSourceDir())
                .build();

        codegen.emit();
    }

    private FeignCodegenExtension getExtension() {
        return getProject().getExtensions()
                .getByType(FeignCodegenExtension.class);
    }

    @TaskAction
    public void run() {
        var extension = getExtension();
        var classloader = resolveClassLoader();

        generate(extension, classloader);
    }

    private ClassLoader resolveClassLoader() {
        var extension = getExtension();

        var sourceProj = extension.getSourceProject();

        var classpath = GradleUtils.mainSourceSet(sourceProj).getOutput()
                .plus(sourceProj.getConfigurations().getByName("compileClasspath"));

        var urls = DefaultClassPath.of(classpath.getFiles())
                .getAsURLArray();

        // NOTE: Using current classloader as parent.
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

}
