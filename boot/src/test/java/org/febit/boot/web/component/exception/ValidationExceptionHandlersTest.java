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

import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlersTest {

    private final ValidationExceptionHandlers handlers = new ValidationExceptionHandlers();

    @SuppressWarnings("unused")
    static class Fixture {
        void m(String s) {
        }
    }

    private static MethodParameter mp() throws NoSuchMethodException {
        return new MethodParameter(Fixture.class.getDeclaredMethod("m", String.class), 0);
    }

    @Test
    void httpMessageNotReadableMapsToBadRequest() {
        var resp = handlers.handler(new HttpMessageNotReadableException("bad", (HttpInputMessage) null));
        assertStatus400(resp);
        assertThat(resp.getMessage()).isEqualTo("sys.request.invalid-message");
    }

    @Test
    void typeMismatchMapsToIllegalArg() throws Exception {
        var ex = new MethodArgumentTypeMismatchException("x", String.class, "param", mp(), null);
        var resp = handlers.handler(ex);
        assertStatus400(resp);
        assertThat(resp.getMessage()).isEqualTo("sys.args.type-not-match");
    }

    @Test
    void notValidMapsToIllegalArgWithFieldMessages() throws Exception {
        var br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "field", "must not be null"));
        var ex = new MethodArgumentNotValidException(mp(), br);
        var resp = handlers.handler(ex);
        assertStatus400(resp);
        assertThat(resp.getMessage()).contains("field").contains("must not be null");
    }

    @Test
    void constraintViolationNullSetMapsToIllegalArg() {
        var resp = handlers.handler(
                new ConstraintViolationException((Set<? extends ConstraintViolation<?>>) null));
        assertStatus400(resp);
        assertThat(resp.getMessage()).isEmpty();
    }

    @Test
    void constraintViolationMapsToIllegalArgWithMessages() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("msg");
        var resp = handlers.handler(new ConstraintViolationException(Set.of(violation)));
        assertStatus400(resp);
        assertThat(resp.getMessage()).contains("field").contains("msg");
    }

    @Test
    void notValidWithGlobalErrorUsesObjectName() throws Exception {
        var br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new ObjectError("obj", "global error"));
        var resp = handlers.handler(new MethodArgumentNotValidException(mp(), br));
        assertStatus400(resp);
        assertThat(resp.getMessage()).contains("obj").contains("global error");
    }

    private static void assertStatus400(IResponse<?> resp) {
        assertThat(resp.getStatus()).isEqualTo(400);
    }
}
