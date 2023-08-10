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
import org.febit.boot.common.util.Errors;
import org.febit.boot.common.util.Priority;
import org.febit.lang.protocol.IResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
@Order(Priority.HIGH)
public class SqlExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public IResponse<Void> handler(SQLIntegrityConstraintViolationException ex) {
        log.info("SQLIntegrityConstraintViolationException: {}", ex.getMessage());
        return Errors.CONSTRAINT_INTEGRITY
                .response("sys.integrity-constraint");
    }
}
