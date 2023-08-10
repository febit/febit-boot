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
package org.febit.boot.web.component;

import org.febit.lang.protocol.IResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ControllerAdvice
public class ResponseResponseBodyAdvice implements ResponseBodyAdvice<IResponse<?>> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            @Nonnull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return IResponse.class
                .isAssignableFrom(returnType.getParameterType());
    }

    @Override
    @Nullable
    public IResponse<?> beforeBodyWrite(
            @Nullable IResponse<?> body,
            @Nonnull MethodParameter returnType,
            @Nonnull MediaType contentType,
            @Nonnull Class<? extends HttpMessageConverter<?>> converterType,
            @Nonnull ServerHttpRequest request,
            @Nonnull ServerHttpResponse response
    ) {
        if (body == null) {
            return null;
        }
        int status = body.getStatus();
        if (status > 0) {
            response.setStatusCode(HttpStatus.valueOf(status));
        }
        return body;
    }
}
