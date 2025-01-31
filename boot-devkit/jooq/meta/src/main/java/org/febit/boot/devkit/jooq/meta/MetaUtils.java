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
package org.febit.boot.devkit.jooq.meta;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;
import org.jooq.util.jaxb.tools.MiniJAXB;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class MetaUtils {

    public static final String JOOQ_PKG = "org.febit.common.jooq";
    public static final String JOOQ_DEVKIT_PKG = "org.febit.boot.devkit.jooq";
    public static final String CORE_PKG = JOOQ_PKG;
    public static final String RUNTIME_PKG = JOOQ_DEVKIT_PKG + ".runtime";
    public static final String CLASS_GENERATOR = RUNTIME_PKG + ".JooqJavaGenerator";
    public static final String CLASS_STRATEGY = RUNTIME_PKG + ".JooqGeneratorStrategy";
    public static final String CODEGEN_JOOQ = "codegenJooq";
    public static final String GROUP_NAME = "codegen";
    public static final String CODEGEN_JOOQ_FOLDER = "codegen-jooq";
    public static final String CLASS_MAIN = "org.jooq.codegen.GenerationTool";

    public static Generator createAndInitGenerator() {
        var g = new Generator();
        initGenerator(g);
        return g;
    }

    public static void initGenerator(Generator g) {
        g.setName(CLASS_GENERATOR);

        computeIfAbsent(g::getDatabase, g::setDatabase, Database::new);
        computeIfAbsent(g::getTarget, g::setTarget, Target::new);
        computeIfAbsent(g::getStrategy, g::setStrategy, Strategy::new);
        computeIfAbsent(g::getGenerate, g::setGenerate, Generate::new);

        var strategy = g.getStrategy();
        strategy.withName(CLASS_STRATEGY);

        initGenerate(g.getGenerate());
    }

    private static <P> void computeIfAbsent(Supplier<P> getter, Consumer<P> setter, Supplier<P> supplier) {
        if (getter.get() == null) {
            setter.accept(supplier.get());
        }
    }

    public static void initGenerate(Generate generate) {
        generate.withPojos(true)
                .withJavaTimeTypes(true)
                .withFluentSetters(false)
                .withJavaBeansGettersAndSetters(true)
                .withJpaAnnotations(true)
                .withValidationAnnotations(true);
    }

    private static <P> void setIfAbsent(Supplier<P> getter, Consumer<P> setter, P defaultValue) {
        if (getter.get() == null) {
            setter.accept(defaultValue);
        }
    }

    public static void emitConfigSilent(Configuration conf, File file) {
        try {
            MetaUtils.emitConfig(conf, file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void emitConfig(Configuration conf, File file) throws IOException {
        var content = MiniJAXB.marshal(conf);
        FileUtils.write(file, content, StandardCharsets.UTF_8);
    }
}
