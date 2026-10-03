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
package org.febit.boot.validation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SizeUtilsTest {

    @Test
    void charSequence() {
        assertThat(SizeUtils.sizeOf("hello")).isEqualTo(5);
    }

    @Test
    void emptyCharSequence() {
        assertThat(SizeUtils.sizeOf("")).isEqualTo(0);
    }

    @Test
    void collection() {
        assertThat(SizeUtils.sizeOf(List.of(1, 2, 3))).isEqualTo(3);
    }

    @Test
    void map() {
        assertThat(SizeUtils.sizeOf(Map.of("a", 1, "b", 2))).isEqualTo(2);
    }

    @Test
    void array() {
        assertThat(SizeUtils.sizeOf(new int[]{1, 2, 3, 4})).isEqualTo(4);
    }

    @Test
    void nullValue() {
        assertThat(SizeUtils.sizeOf(null)).isEqualTo(-1);
    }

    @Test
    void unsupported() {
        assertThat(SizeUtils.sizeOf(new Object())).isEqualTo(-1);
    }

    @Test
    void nonStringCharSequence() {
        assertThat(SizeUtils.sizeOf(new StringBuilder("hello"))).isEqualTo(5);
    }

    @Test
    void setCollection() {
        assertThat(SizeUtils.sizeOf(Set.of(1, 2, 3))).isEqualTo(3);
    }
}
