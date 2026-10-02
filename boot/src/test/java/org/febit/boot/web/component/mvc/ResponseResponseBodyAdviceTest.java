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
package org.febit.boot.web.component.mvc;

import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ResponseResponseBodyAdviceTest {

    private final ResponseResponseBodyAdvice advice = new ResponseResponseBodyAdvice();

    @Test
    void supportsIResponseParameter() throws Exception {
        var param = new MethodParameter(iResponseMethod("iResponseParam"), 0);
        assertThat(advice.supports(param, null)).isTrue();
    }

    @Test
    void doesNotSupportNonIResponseParameter() throws Exception {
        var param = new MethodParameter(iResponseMethod("stringParam"), 0);
        assertThat(advice.supports(param, null)).isFalse();
    }

    @Test
    void setsStatusForIResponseBody() {
        var body = IResponse.failed(404, "NF", "not found");
        var written = write(body);

        assertThat(written.body()).isSameAs(body);
        var captured = forClass(HttpStatus.class);
        verify(written.response()).setStatusCode(captured.capture());
        assertThat(captured.getValue()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void doesNotSetStatusForNonIResponseBody() {
        var written = write("plain");

        assertThat(written.body()).isEqualTo("plain");
        verifyNoInteractions(written.response());
    }

    @Test
    void doesNotSetStatusWhenBodyNull() {
        var written = write(null);

        assertThat(written.body()).isNull();
        verifyNoInteractions(written.response());
    }

    @Test
    void doesNotSetStatusWhenUnmapped() {
        verifyNoInteractions(write(IResponse.failed(700, "X", "msg")).response());
    }

    private record Written(Object body, ServerHttpResponse response) {
    }

    private Written write(Object body) {
        var request = mock(ServerHttpRequest.class);
        var response = mock(ServerHttpResponse.class);
        var result = advice.beforeBodyWrite(body, null, null, null, request, response);
        return new Written(result, response);
    }

    private static Method iResponseMethod(String name) throws NoSuchMethodException {
        return ResponseResponseBodyAdviceTest.class.getDeclaredMethod(name,
                "iResponseParam".equals(name) ? IResponse.class : String.class);
    }

    @SuppressWarnings("unused")
    private void iResponseParam(IResponse<?> r) {
    }

    @SuppressWarnings("unused")
    private void stringParam(String s) {
    }
}
