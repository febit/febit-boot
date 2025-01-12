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

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.util.Errors;
import org.febit.boot.util.Priority;
import org.febit.lang.protocol.IResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@Order(Priority.HIGH)
public class ValidationExceptionHandlers {

    private static String resolveField(ObjectError error) {
        if (error instanceof FieldError) {
            return ((FieldError) error).getField();
        }
        return error.getObjectName();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public IResponse<Void> handler(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadable", ex);
        return Errors.BAD_REQUEST
                .response("sys.request.invalid-message");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public IResponse<Void> handler(MethodArgumentTypeMismatchException ex) {
        return Errors.ILLEGAL_ARG
                .response("sys.args.type-not-match", ex.getParameter().getParameterName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public IResponse<Void> handler(MethodArgumentNotValidException ex) {
        StringBuilder buf = new StringBuilder();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            buf.append(resolveField(error))
                    .append(": ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }
        return Errors.ILLEGAL_ARG
                .response(buf.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public IResponse<Void> handler(ConstraintViolationException ex) {
        var buf = new StringBuilder();
        if (ex.getConstraintViolations() != null) {
            for (var violation : ex.getConstraintViolations()) {
                buf.append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("; ");
            }
        }
        return Errors.ILLEGAL_ARG
                .response(buf.toString());
    }
}
