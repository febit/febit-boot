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
import org.mockito.ArgumentCaptor;
import org.tabletest.junit.TableTest;

import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdsValidatorTest {

    private final IdsValidator validator = new IdsValidator();

    private ConstraintValidatorContext mockContext() {
        var context = mock(ConstraintValidatorContext.class);
        var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        return context;
    }

    private Ids ids(int min, int max) {
        var annotation = mock(Ids.class);
        when(annotation.min()).thenReturn(min);
        when(annotation.max()).thenReturn(max);
        return annotation;
    }

    @Test
    void invalidWhenNull() {
        validator.initialize(ids(1, 500));
        assertThat(validator.isValid(null, mockContext())).isFalse();
    }

    @TableTest("""
        scenario            | input          | valid | template
        within bounds       | [1, 2, 3]      | true  |
        exactly min         | [1, 2]         | true  |
        exactly max         | [1, 2, 3, 4, 5]| true  |
        below min           | [1]            | false | Size of ids should not be less than {min}
        above max           | [1, 2, 3, 4, 5, 6] | false | Size of ids should not be greater than {max}
        """)
    void isValid(Object[] input, boolean valid, String template) {
        validator.initialize(ids(2, 5));
        var context = mockContext();
        assertThat(validator.isValid(input, context)).isEqualTo(valid);
        if (template != null) {
            var captured = ArgumentCaptor.forClass(String.class);
            verify(context).buildConstraintViolationWithTemplate(captured.capture());
            assertThat(captured.getValue()).isEqualTo(template);
        }
    }

    @Test
    void invalidWhenContainsNull() {
        validator.initialize(ids(1, 500));
        var context = mockContext();
        assertThat(validator.isValid(new Object[]{1, null, 3}, context)).isFalse();

        var captured = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(captured.capture());
        assertThat(captured.getValue()).isEqualTo("Ids should not contain null elements");
    }
}
