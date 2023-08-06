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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import lombok.Setter;
import org.febit.boot.devkit.feign.util.ClassNamingImpl;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeignCodegenExtension {

    @Getter
    private final Client client = new Client();

    @Getter
    private final Naming naming = new Naming();

    @Getter
    @Setter
    private Project sourceProject;

    @Getter
    @Setter
    private File targetSourceDir;

    @Getter
    private final Set<String> scanPackages = new HashSet<>();

    @Getter
    private final Set<String> excludedClasses = new HashSet<>();

    @Inject
    public FeignCodegenExtension(Project project) {
        this.targetSourceDir = new File(project.getBuildDir(),
                "generated/sources/codegen-feign");
    }

    public void scan(String pkg) {
        scanPackages.add(pkg);
    }

    public void excludeClass(String cls) {
        excludedClasses.add(cls);
    }

    public void client(@DelegatesTo(Client.class) Closure<?> closure) {
        GradleUtils.to(closure, this.client);
    }

    public void naming(@DelegatesTo(Naming.class) Closure<?> closure) {
        GradleUtils.to(closure, this.naming);
    }

    public static class Naming {

        @Getter
        private final ClassNamingImpl global = new ClassNamingImpl();

        @Getter
        private final ClassNamingImpl client = new ClassNamingImpl();

        @Getter
        private final ClassNamingImpl pojo = new ClassNamingImpl();

        public void global(@DelegatesTo(ClassNamingImpl.class) Closure<?> closure) {
            GradleUtils.to(closure, this.global);
        }

        public void pojo(@DelegatesTo(ClassNamingImpl.class) Closure<?> closure) {
            GradleUtils.to(closure, this.pojo);
        }

        public void client(@DelegatesTo(ClassNamingImpl.class) Closure<?> closure) {
            GradleUtils.to(closure, this.client);
        }
    }

    public static class Client {
        @Getter
        @Setter
        private String name = "";

        @Getter
        @Setter
        private String url = "";

        @Getter
        @Setter
        private String basePackage;

        @Getter
        private final List<String> configClasses = new ArrayList<>();

        public void configClass(String cls) {
            configClasses.add(cls);
        }
    }
}
