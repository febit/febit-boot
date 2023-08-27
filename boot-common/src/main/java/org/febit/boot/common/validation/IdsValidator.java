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
package org.febit.boot.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdsValidator implements ConstraintValidator<Ids, Object[]> {

    private int min;
    private int max;

    @Override
    public void initialize(Ids ids) {
        this.min = ids.min();
        this.max = ids.max();
    }

    @Override
    public boolean isValid(Object[] ids, ConstraintValidatorContext context) {
        if (ids == null) {
            return false;
        }
        if (ids.length < min) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Size of ids should not less than " + min
                    )
                    .addConstraintViolation();
            return false;
        }
        if (ids.length > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Size of ids should not greater than " + max
                    )
                    .addConstraintViolation();
            return false;
        }
        for (var id : ids) {
            if (id != null) {
                continue;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Ids should not contains null elements"
                    )
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
