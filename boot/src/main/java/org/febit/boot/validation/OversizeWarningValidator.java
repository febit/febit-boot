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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

/**
 * Never rejects; logging only (see {@link OversizeWarning}).
 */
public class OversizeWarningValidator implements ConstraintValidator<OversizeWarning, Object> {

    private static final Logger log = LoggerFactory.getLogger(OversizeWarningValidator.class);

    private int warn;
    private int error;

    @Override
    public void initialize(OversizeWarning anno) {
        this.warn = anno.warn();
        this.error = anno.error();
    }

    @Override
    public boolean isValid(@Nullable Object value, ConstraintValidatorContext context) {
        var size = SizeUtils.sizeOf(value);
        var level = size < 0 ? 0 : levelOf(size);
        if (level == 2) {
            log.error("[OversizeWarning] exceeded ERROR threshold: size={} error={} warn={}", size, error, warn);
        } else if (level == 1) {
            log.warn("[OversizeWarning] exceeded WARN threshold: size={} warn={} error={}", size, warn, error);
        }
        return true;
    }

    /**
     * @return 2 if {@code size} exceeds the ERROR threshold, 1 if it exceeds WARN,
     *         or 0 otherwise. A negative threshold ({@code -1}) disables that level.
     */
    int levelOf(int size) {
        if (error >= 0 && size > error) {
            return 2;
        }
        if (warn >= 0 && size > warn) {
            return 1;
        }
        return 0;
    }
}
