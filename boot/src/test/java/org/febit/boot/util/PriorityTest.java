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
package org.febit.boot.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityTest {

    @Test
    void constants() {
        assertThat(Priority.HIGHEST).isEqualTo(Integer.MIN_VALUE);
        assertThat(Priority.HIGHER).isEqualTo(-Priority.LOWER);
        assertThat(Priority.HIGH).isEqualTo(-Priority.LOW);
        assertThat(Priority.NORMAL).isZero();
        assertThat(Priority.LOW).isEqualTo(1024);
        assertThat(Priority.LOWER).isEqualTo(65_536);
        assertThat(Priority.LOWEST).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void orderedAscending() {
        assertThat(Priority.HIGHEST)
                .isLessThan(Priority.HIGHER)
                .isLessThan(Priority.HIGH)
                .isLessThan(Priority.NORMAL)
                .isLessThan(Priority.LOW)
                .isLessThan(Priority.LOWER)
                .isLessThan(Priority.LOWEST);
    }
}
