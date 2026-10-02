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

import com.fasterxml.classmate.ResolvedType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class ExtraEnumMetaPropertyCustomizerTest {

    private static final String X_ENUM_CLASS = "x-enum-class";
    private static final String X_ENUM_FULL_CLASS = "x-enum-full-class";

    private final ExtraEnumMetaPropertyCustomizer customizer = new ExtraEnumMetaPropertyCustomizer();

    private Schema customize(Schema schema, Type type) {
        return customizer.customize(schema, new AnnotatedType().type(type));
    }

    private static Schema enumSchema(Object... values) {
        var schema = new Schema();
        schema.setEnum(List.of(values));
        return schema;
    }

    @Nested
    class WithoutEnumValues {
        @Test
        void returnsSchemaUnchanged() {
            var schema = new Schema();

            var result = customize(schema, MyEnum.class);

            assertThat(result).isSameAs(schema);
            assertThat(result.getExtensions()).isNullOrEmpty();
        }
    }

    @Nested
    class WithEnumClassType {
        @Test
        void addsBothClassExtensionsForMyEnum() {
            var result = customize(enumSchema("A", "B"), MyEnum.class);

            var extensions = result.getExtensions();
            assertThat(extensions).containsOnlyKeys(X_ENUM_CLASS, X_ENUM_FULL_CLASS);
            assertThat(extensions.get(X_ENUM_CLASS)).isEqualTo(MyEnum.class.getSimpleName());
            assertThat(extensions.get(X_ENUM_FULL_CLASS)).isEqualTo(MyEnum.class.getCanonicalName());
        }

        @Test
        void addsBothClassExtensionsForString() {
            var result = customize(enumSchema("A", "B"), String.class);

            var extensions = result.getExtensions();
            assertThat(extensions).containsOnlyKeys(X_ENUM_CLASS, X_ENUM_FULL_CLASS);
            assertThat(extensions.get(X_ENUM_CLASS)).isEqualTo(String.class.getSimpleName());
            assertThat(extensions.get(X_ENUM_FULL_CLASS)).isEqualTo(String.class.getCanonicalName());
        }

        @Test
        void addsBothExtensionsWhenTypeWrappedInClassmateResolvedType() {
            var resolved = mock(ResolvedType.class, withSettings().extraInterfaces(Type.class));
            doReturn(MyEnum.class).when((ResolvedType) resolved).getErasedType();

            var result = customize(enumSchema("A"), (Type) resolved);

            var extensions = result.getExtensions();
            assertThat(extensions).containsOnlyKeys(X_ENUM_CLASS, X_ENUM_FULL_CLASS);
            assertThat(extensions.get(X_ENUM_CLASS)).isEqualTo("MyEnum");
            assertThat(extensions.get(X_ENUM_FULL_CLASS)).isEqualTo(MyEnum.class.getCanonicalName());
        }
    }

    @Nested
    class WithNonClassType {
        @Test
        void addsOnlyFullClassExtensionForParameterizedType() throws Exception {
            Type generic = ExtraEnumMetaPropertyCustomizerTest.class
                    .getDeclaredField("genericField").getGenericType();

            var result = customize(enumSchema("A"), generic);

            var extensions = result.getExtensions();
            assertThat(extensions).containsOnlyKeys(X_ENUM_FULL_CLASS);
            assertThat(extensions.get(X_ENUM_FULL_CLASS)).isEqualTo(generic.getTypeName());
            assertThat(extensions.get(X_ENUM_CLASS)).isNull();
        }
    }

    @Nested
    class Unwrap {
        @Test
        void keepsPlainClass() {
            assertThat(ExtraEnumMetaPropertyCustomizer.unwrap(MyEnum.class)).isEqualTo(MyEnum.class);
        }

        @Test
        void keepsNonClassType() throws Exception {
            Type generic = ExtraEnumMetaPropertyCustomizerTest.class
                    .getDeclaredField("genericField").getGenericType();
            assertThat(ExtraEnumMetaPropertyCustomizer.unwrap(generic)).isEqualTo(generic);
        }

        @Test
        void unwrapsClassmateResolvedType() {
            var resolved = mock(ResolvedType.class, withSettings().extraInterfaces(Type.class));
            doReturn(MyEnum.class).when((ResolvedType) resolved).getErasedType();
            assertThat(ExtraEnumMetaPropertyCustomizer.unwrap((Type) resolved)).isEqualTo(MyEnum.class);
        }

        @Test
        void unwrapsJackson2ResolvedType() {
            var resolved = mock(com.fasterxml.jackson.core.type.ResolvedType.class,
                    withSettings().extraInterfaces(Type.class));
            doReturn(MyEnum.class).when((com.fasterxml.jackson.core.type.ResolvedType) resolved).getRawClass();
            assertThat(ExtraEnumMetaPropertyCustomizer.unwrap((Type) resolved)).isEqualTo(MyEnum.class);
        }

        @Test
        void unwrapsJackson3ResolvedType() {
            var resolved = mock(tools.jackson.core.type.ResolvedType.class,
                    withSettings().extraInterfaces(Type.class));
            doReturn(MyEnum.class).when((tools.jackson.core.type.ResolvedType) resolved).getRawClass();
            assertThat(ExtraEnumMetaPropertyCustomizer.unwrap((Type) resolved)).isEqualTo(MyEnum.class);
        }
    }

    @SuppressWarnings("unused")
    private static List<String> genericField;

    private enum MyEnum {
        A, B
    }
}
