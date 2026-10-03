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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Map;

public class EachSizeValidator implements ConstraintValidator<EachSize, Object> {

    private int min;
    private int max;

    @Override
    public void initialize(EachSize anno) {
        this.min = anno.min();
        this.max = anno.max();
    }

    @Override
    public boolean isValid(@Nullable Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                if (!isValidElement(entry.getValue())) {
                    report(context, entry.getKey());
                    return false;
                }
            }
            return true;
        }
        if (value instanceof Iterable<?> iterable) {
            int index = 0;
            for (var element : iterable) {
                if (!isValidElement(element)) {
                    report(context, index);
                    return false;
                }
                index++;
            }
            return true;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                if (!isValidElement(Array.get(value, index))) {
                    report(context, index);
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    private void report(@Nullable ConstraintValidatorContext context, Object node) {
        if (context == null) {
            return;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(String.valueOf(node))
                .addConstraintViolation();
    }

    private boolean isValidElement(@Nullable Object element) {
        var size = SizeUtils.sizeOf(element);
        return size < 0 || (size >= min && size <= max);
    }
}
