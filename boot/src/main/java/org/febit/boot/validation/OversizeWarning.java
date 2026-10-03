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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Never rejects; logs above {@link #warn()} / {@link #error()}.
 * A negative threshold ({@code -1}) disables that level (default: off).
 *
 * @see SizeUtils
 * @see OversizeWarningValidator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OversizeWarningValidator.class)
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
public @interface OversizeWarning {

    int warn() default -1;

    int error() default -1;

    String message() default "Oversize warning threshold exceeded (warn={warn}, error={error})";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
