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
package org.febit.boot.springdoc;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraMetaPropertyCustomizerTest {

    private static final String X_CLASS = "x-class";

    private final ExtraMetaPropertyCustomizer customizer = new ExtraMetaPropertyCustomizer();

    private Schema customize(Schema schema, Type type) {
        return customizer.customize(schema, new AnnotatedType().type(type));
    }

    @Test
    void addsXClassExtensionForString() {
        assertXClassExtension(String.class, "String");
    }

    @Test
    void addsXClassExtensionForSample() {
        assertXClassExtension(Sample.class, "Sample");
    }

    @Test
    void addsXClassExtensionForList() {
        assertXClassExtension(List.class, "List");
    }

    @Test
    void addsXClassExtensionForGenericField() throws Exception {
        Type generic = ExtraMetaPropertyCustomizerTest.class
                .getDeclaredField("genericField").getGenericType();
        assertXClassExtension(generic, "List");
    }

    @Test
    void addsXClassExtensionForStringArray() {
        assertXClassExtension(String[].class, "String[]");
    }

    @Test
    void addsXClassExtensionForPrimitiveInt() {
        assertXClassExtension(int.class, "int");
    }

    private void assertXClassExtension(Type type, String expectedSimpleName) {
        Schema result = customize(new Schema(), type);

        var extensions = result.getExtensions();
        assertThat(extensions).containsOnlyKeys(X_CLASS);
        assertThat(extensions.get(X_CLASS)).isEqualTo(expectedSimpleName);
    }

    @SuppressWarnings("unused")
    private static List<String> genericField;

    @SuppressWarnings("unused")
    static class Sample {
    }
}
