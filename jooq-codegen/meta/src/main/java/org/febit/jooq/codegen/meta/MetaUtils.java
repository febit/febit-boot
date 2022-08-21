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

@UtilityClass
public class MetaUtils {

    public static final String CODEGEN_JOOQ = "codegenJooq";
    public static final String GROUP_NAME = "Codegen jOOQ";
    public static final String CODEGEN_JOOQ_FOLDER = "codegen-jooq";
    public static final String CLASS_GENERATOR = "org.febit.jooq.codegen.JooqJavaGenerator";
    public static final String CLASS_STRATEGY = "org.febit.jooq.codegen.JooqGeneratorStrategy";
    public static final String CLASS_MAIN = "org.jooq.codegen.GenerationTool";

    public Generator createGenerator() {
        return new Generator()
                .withName(CLASS_GENERATOR)
                .withDatabase(new Database())
                .withTarget(new Target())
                .withStrategy(new Strategy()
                        .withName(CLASS_STRATEGY)
                )
                .withGenerate(new Generate()
                        .withPojos(true)
                        .withFluentSetters(false)
                        .withImmutablePojos(false)
                        .withJavaTimeTypes(true)
                        .withJavaBeansGettersAndSetters(true)
                        .withJpaAnnotations(true)
                        .withValidationAnnotations(true)
                );
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
