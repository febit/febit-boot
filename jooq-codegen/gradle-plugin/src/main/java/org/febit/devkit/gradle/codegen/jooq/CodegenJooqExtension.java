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
package org.febit.devkit.gradle.codegen.jooq;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.jooq.codegen.meta.MetaUtils;
import org.gradle.api.Project;
import org.jooq.meta.jaxb.*;

import javax.inject.Inject;

public class CodegenJooqExtension {

    @Getter
    private final ForcedTypesHandler forcedTypes;
    @Getter
    private final Generator generator;
    @Getter
    private final Jdbc jdbc;

    @Inject
    public CodegenJooqExtension(Project project) {
        this.jdbc = new Jdbc();
        this.forcedTypes = new ForcedTypesHandler();
        this.generator = MetaUtils.createAndInitGenerator();

        this.generator.getDatabase()
                .withForcedTypes(this.forcedTypes);
        this.generator.getTarget()
                .withDirectory(CodegenJooqPlugin.DIR_GENERATED_SRC);
    }

    public Configuration getJooqConfig() {
        var conf = new Configuration();
        conf.setJdbc(this.jdbc);
        conf.setGenerator(this.generator);
        return conf;
    }

    public String getTargetDir() {
        return this.generator
                .getTarget()
                .getDirectory();
    }

    public void forcedTypes(@DelegatesTo(ForcedTypesHandler.class) Closure<?> closure) {
        GradleUtils.to(closure, this.forcedTypes);
    }

    public void setSchema(String schema) {
        this.generator.getDatabase()
                .setInputSchema(schema);
    }

    public void setIncludeSchema(String schema) {
        SchemaMappingType type = new SchemaMappingType();
        type.setInputSchema(schema);
        this.generator.getDatabase().getSchemata().add(type);
    }

    public void setExcludes(String excludes) {
        this.generator.getDatabase().setExcludes(excludes);
    }

    public void setTargetPackage(String name) {
        this.generator.getTarget().setPackageName(name);
    }

    public void setGenerate(@DelegatesTo(Generate.class) Closure<?> closure) {
        GradleUtils.to(closure, this.generator.getGenerate());
    }

    public void presetJdbc(@DelegatesTo(Jdbc.class) Closure<?> closure) {
        GradleUtils.to(closure, this.jdbc);
    }
}
