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
package org.febit.boot.auth.web;

import org.febit.boot.auth.AuthSubject;
import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthHandlerInterceptorTest {

    private final WebAuthHandler<AuthSubject> authHandler = mock(WebAuthHandler.class);
    private final AuthHandlerInterceptor<AuthSubject> interceptor = AuthHandlerInterceptor.create(authHandler);

    @Test
    void passesThroughForPlainObjectTarget() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        verifyNoInteractions(authHandler);
    }

    @Test
    void passesThroughForResourceHandlerTarget() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new ResourceHttpRequestHandler())).isTrue();
        verifyNoInteractions(authHandler);
    }

    @Test
    void passesThroughWhenVerifySucceeds() throws Exception {
        doReturn(IResponse.ok(mock(AuthSubject.class))).when(authHandler).verify(any(), any());
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handlerMethod());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEmpty();
        verify(authHandler).verify(any(), any());
    }

    @Test
    void writesFailureResponseWhenVerifyFails() throws Exception {
        var failed = IResponse.failed(401, "UNAUTHORIZED", "no permission");
        doReturn(failed).when(authHandler).verify(any(), any());
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handlerMethod());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
        verify(authHandler).verify(any(), any());
    }

    private HandlerMethod handlerMethod() throws Exception {
        Method method = AuthHandlerInterceptorTest.class.getDeclaredMethod("handler");
        return new HandlerMethod(this, method);
    }

    @SuppressWarnings("unused")
    private void handler() {
    }
}
