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
package org.febit.boot.web.component.exception;

import lombok.extern.slf4j.Slf4j;
import org.febit.boot.util.Priority;
import org.febit.lang.protocol.BusinessException;
import org.febit.lang.protocol.IResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
@Order(Priority.HIGHER)
public class BusinessExceptionHandler {

    private static final String[] EXCLUDE_PKG_PREFIXES = {
            "sun.",
            "java.",
            "javax.",
            "jakarta.",
            "org.springframework.",
    };

    private static void shortenStackTrace(BusinessException ex) {
        var stack = ex.getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            var className = stack[i].getClassName();
            for (var prefix : EXCLUDE_PKG_PREFIXES) {
                if (className.startsWith(prefix)) {
                    ex.setStackTrace(Arrays.copyOf(stack, i));
                    return;
                }
            }
        }
    }

    @ExceptionHandler(BusinessException.class)
    public IResponse<Void> handler(BusinessException ex) {
        if (log.isDebugEnabled()) {
            if (!log.isTraceEnabled()) {
                try {
                    shortenStackTrace(ex);
                } catch (Exception ignore) {
                    // Ignore
                }
            }
            log.debug("Business exception:", ex);
        }
        return ex.toResponse();
    }
}
