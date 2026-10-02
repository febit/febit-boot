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
import org.febit.common.jooq.Column;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JooqSearchFormPropertyCustomizerTest {

    private static final String X_SEARCH_FILTER = "x-search-filter";

    @Column
    @SuppressWarnings("unused")
    private String withColumn;

    @Deprecated
    @SuppressWarnings("unused")
    private String notColumn;

    private Schema customize(Annotation[] annotations) {
        var annotatedType = new AnnotatedType().type(String.class);
        if (annotations != null) {
            annotatedType.ctxAnnotations(annotations);
        }
        return new JooqSearchFormPropertyCustomizer().customize(new Schema(), annotatedType);
    }

    @Nested
    class WithoutColumn {
        @Test
        void returnsUnchangedWhenNoAnnotations() {
            Schema result = customize(null);
            assertThat(result.getExtensions()).isNullOrEmpty();
        }

        @Test
        void returnsUnchangedWhenAnnotationIsNotColumn() throws Exception {
            Annotation anno = field("notColumn").getAnnotation(Deprecated.class);
            Schema result = customize(new Annotation[]{anno});
            assertThat(result.getExtensions()).isNullOrEmpty();
        }
    }

    @Nested
    class WithColumn {
        @Test
        void addsSearchFilterExtensionWithOperatorName() throws Exception {
            Column colAnno = field("withColumn").getAnnotation(Column.class);
            Schema result = customize(new Annotation[]{colAnno});

            var extensions = result.getExtensions();
            assertThat(extensions).containsOnlyKeys(X_SEARCH_FILTER);
            assertThat(extensions.get(X_SEARCH_FILTER)).isEqualTo(colAnno.operator().name());
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        return JooqSearchFormPropertyCustomizerTest.class.getDeclaredField(name);
    }
}
