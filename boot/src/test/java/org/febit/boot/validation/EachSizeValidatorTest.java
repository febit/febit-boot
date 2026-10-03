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

import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EachSizeValidatorTest {

    private final EachSizeValidator validator = new EachSizeValidator();

    private EachSize eachSize(int min, int max) {
        var anno = mock(EachSize.class);
        when(anno.min()).thenReturn(min);
        when(anno.max()).thenReturn(max);
        return anno;
    }

    private ConstraintValidatorContext reportingContext() {
        var context = mock(ConstraintValidatorContext.class);
        var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        when(builder.addPropertyNode(any())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
        return context;
    }

    @Test
    void nullValueIsValid() {
        validator.initialize(eachSize(0, 10));
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void topLevelNonContainerIsValid() {
        validator.initialize(eachSize(0, 5));
        assertThat(validator.isValid("a plain string", null)).isTrue();
        assertThat(validator.isValid(42, null)).isTrue();
    }

    @Test
    void emptyContainerIsValid() {
        validator.initialize(eachSize(1, 5));
        assertThat(validator.isValid(List.of(), null)).isTrue();
        assertThat(validator.isValid(Map.of(), null)).isTrue();
    }

    @Test
    void charSequenceElementWithinBounds() {
        validator.initialize(eachSize(0, 3));
        assertThat(validator.isValid(List.of("ab", "abc"), null)).isTrue();
    }

    @Test
    void charSequenceElementExceedingMaxIsInvalid() {
        validator.initialize(eachSize(0, 3));
        assertThat(validator.isValid(List.of("abcd"), null)).isFalse();
    }

    @Test
    void charSequenceElementBelowMinIsInvalid() {
        validator.initialize(eachSize(2, 5));
        assertThat(validator.isValid(List.of("a"), null)).isFalse();
    }

    @Test
    void charSequenceElementAtMinIsValid() {
        validator.initialize(eachSize(2, 5));
        assertThat(validator.isValid(List.of("ab"), null)).isTrue();
    }

    @Test
    void nullElementIsSkipped() {
        validator.initialize(eachSize(1, 5));
        assertThat(validator.isValid(Arrays.asList(null, "abc"), null)).isTrue();
    }

    @Test
    void unsupportedElementIsSkipped() {
        validator.initialize(eachSize(0, 1));
        assertThat(validator.isValid(List.of(42), null)).isTrue();
    }

    @Test
    void nestedCollectionElement() {
        validator.initialize(eachSize(0, 2));
        assertThat(validator.isValid(List.of(List.of(1, 2, 3)), null)).isFalse();
        validator.initialize(eachSize(0, 3));
        assertThat(validator.isValid(List.of(List.of(1, 2, 3)), null)).isTrue();
    }

    @Test
    void mapValueElement() {
        validator.initialize(eachSize(0, 1));
        assertThat(validator.isValid(Map.of("a", Map.of("x", 1, "y", 2)), null)).isFalse();
        validator.initialize(eachSize(0, 2));
        assertThat(validator.isValid(Map.of("a", Map.of("x", 1, "y", 2)), null)).isTrue();
    }

    @Test
    void mapFieldValues() {
        validator.initialize(eachSize(0, 5));
        assertThat(validator.isValid(Map.of("k", "short", "k2", "toolongvalue"), null)).isFalse();
    }

    @Test
    void arrayElement() {
        validator.initialize(eachSize(0, 3));
        assertThat(validator.isValid(List.of(new int[]{1, 2, 3, 4}), null)).isFalse();
        validator.initialize(eachSize(0, 4));
        assertThat(validator.isValid(List.of(new int[]{1, 2, 3, 4}), null)).isTrue();
    }

    @Test
    void arrayContainer() {
        validator.initialize(eachSize(0, 3));
        assertThat(validator.isValid(new String[]{"ab", "abc"}, null)).isTrue();
        assertThat(validator.isValid(new String[]{"abcd"}, null)).isFalse();
    }

    @Test
    void reportsIterableIndexOnViolation() {
        validator.initialize(eachSize(0, 2));
        var context = reportingContext();
        assertThat(validator.isValid(List.of("ok", "toolong"), context)).isFalse();
        verify(context).disableDefaultConstraintViolation();
        var index = ArgumentCaptor.forClass(String.class);
        verify(context.buildConstraintViolationWithTemplate(any())).addPropertyNode(index.capture());
        assertThat(index.getValue()).isEqualTo("1");
    }

    @Test
    void reportsMapKeyOnViolation() {
        validator.initialize(eachSize(0, 1));
        var context = reportingContext();
        assertThat(validator.isValid(Map.of("bad", "toolongvalue"), context)).isFalse();
        verify(context).disableDefaultConstraintViolation();
        var key = ArgumentCaptor.forClass(String.class);
        verify(context.buildConstraintViolationWithTemplate(any())).addPropertyNode(key.capture());
        assertThat(key.getValue()).isEqualTo("bad");
    }

    @Test
    void reportsArrayIndexOnViolation() {
        validator.initialize(eachSize(0, 2));
        var context = reportingContext();
        assertThat(validator.isValid(new String[]{"ok", "toolong"}, context)).isFalse();
        verify(context).disableDefaultConstraintViolation();
        var index = ArgumentCaptor.forClass(String.class);
        verify(context.buildConstraintViolationWithTemplate(any())).addPropertyNode(index.capture());
        assertThat(index.getValue()).isEqualTo("1");
    }
}
