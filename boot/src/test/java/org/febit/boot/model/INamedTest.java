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
package org.febit.boot.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class INamedTest {

    static class Named implements INamed {
        private final String name;

        Named(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Test
    void isNameEqualsByName() {
        var named = new Named("a");
        assertThat(named.isNameEquals("a")).isTrue();
        assertThat(named.isNameEquals("b")).isFalse();
        assertThat(named.isNameEquals((String) null)).isFalse();
    }

    @Test
    void isNameEqualsByOther() {
        var named = new Named("a");
        assertThat(named.isNameEquals(new Named("a"))).isTrue();
        assertThat(named.isNameEquals(new Named("b"))).isFalse();
        assertThat(named.isNameEquals((INamed) null)).isFalse();
    }
}
