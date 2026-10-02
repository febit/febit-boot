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
package org.febit.boot.web.util;

import io.swagger.v3.core.converter.AnnotatedType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringdocUtilsTest {

    @SuppressWarnings("unused")
    private List<String> genericField;

    @Test
    void extractsRawClassOfPlainType() {
        assertThat(SpringdocUtils.extractRawClass(new AnnotatedType().type(String.class)))
                .isEqualTo(String.class);
    }

    @Test
    void extractsRawClassOfGenericType() {
        assertThat(SpringdocUtils.extractRawClass(new AnnotatedType().type(List.class)))
                .isEqualTo(List.class);
    }

    @Test
    void extractsRawClassOfArrayType() {
        assertThat(SpringdocUtils.extractRawClass(new AnnotatedType().type(String[].class)))
                .isEqualTo(String[].class);
    }

    @Test
    void extractsRawClassOfReflectedGenericType() throws Exception {
        var field = getClass().getDeclaredField("genericField");
        assertThat(SpringdocUtils.extractRawClass(new AnnotatedType().type(field.getGenericType())))
                .isEqualTo(List.class);
    }
}
