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
package org.febit.jooq.codegen.meta;

import lombok.experimental.UtilityClass;
import org.jooq.meta.jaxb.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class MetaUtils {

    public static final String JOOQ_PKG = "org.febit.jooq";
    public static final String CORE_PKG = JOOQ_PKG;
    public static final String CODEGEN_PKG = JOOQ_PKG + ".codegen";
    public static final String CLASS_GENERATOR = CODEGEN_PKG + ".JooqJavaGenerator";
    public static final String CLASS_STRATEGY = CODEGEN_PKG + ".JooqGeneratorStrategy";
    public static final String CODEGEN_JOOQ = "codegenJooq";
    public static final String GROUP_NAME = "Codegen jOOQ";
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
        } catch (JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static void emitConfig(Configuration conf, File file) throws IOException, JAXBException, SAXException {
        var marshaller = JAXBContext.newInstance(Configuration.class)
                .createMarshaller();

        var schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(org.jooq.meta.Constants.class.getResource("/xsd/" + org.jooq.meta.Constants.XSD_CODEGEN));
        marshaller.setSchema(schema);

        try (var out = new FileOutputStream(file)) {
            marshaller.marshal(conf, out);
        }
    }
}
