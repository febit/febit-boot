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
import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OversizeWarningValidatorTest {

    private final OversizeWarningValidator validator = new OversizeWarningValidator();

    private OversizeWarning oversize(int warn, int error) {
        var anno = mock(OversizeWarning.class);
        when(anno.warn()).thenReturn(warn);
        when(anno.error()).thenReturn(error);
        return anno;
    }

    @Test
    void neverRejects() {
        // 小阈值便于在同一尺度覆盖 低于/介于/超出 三档：OversizeWarning 只告警不拒绝，
        // 即便 size 远超 error 阈值（反例）也返回 true
        validator.initialize(oversize(4, 16));
        assertThat(validator.isValid("x", null)).isTrue();
        assertThat(validator.isValid("xxxxxxx", null)).isTrue();
        assertThat(validator.isValid("x".repeat(50), null)).isTrue();
        assertThat(validator.isValid(List.of(1, 2, 3), null)).isTrue();
        assertThat(validator.isValid(Map.of("a", 1, "b", 2), null)).isTrue();
        assertThat(validator.isValid(new int[]{1, 2, 3, 4}, null)).isTrue();
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void logsWarnWhenExceedingWarnThresholdOnly() {
        validator.initialize(oversize(4, 16));
        assertThat(validator.isValid("x".repeat(10), null)).isTrue();
    }

    @Test
    void logsErrorWhenExceedingErrorThreshold() {
        validator.initialize(oversize(4, 16));
        assertThat(validator.isValid("x".repeat(50), null)).isTrue();
    }

    @TableTest("""
        warn | error | size | expectLevel
        4    | 16    | 10   | 1
        4    | 16    | 50   | 2
        4    | 16    | 1    | 0
        -1   | -1    | 50   | 0
        -1   | 16    | 50   | 2
        -1   | 16    | 1    | 0
        4    | -1    | 10   | 1
        4    | -1    | 50   | 1
        4    | -1    | 1    | 0
        """)
    void levelOf(int warn, int error, int size, int expectLevel) {
        var anno = mock(OversizeWarning.class);
        when(anno.warn()).thenReturn(warn);
        when(anno.error()).thenReturn(error);
        validator.initialize(anno);
        assertThat(validator.levelOf(size)).isEqualTo(expectLevel);
    }
}
