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
package org.febit.boot.web.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.auth.WebAuthHandler;
import org.febit.boot.common.auth.AuthSubject;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.JacksonUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor(staticName = "create")
public class AuthHandlerInterceptor<T extends AuthSubject> implements HandlerInterceptor {

    private final WebAuthHandler<T> authHandler;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            if (!(handler instanceof ResourceHttpRequestHandler)) {
                log.warn("Unsupported handler: {}", handler.getClass());
            }
            return true;
        }
        var webRequest = new ServletWebRequest(request, response);
        var method = ((HandlerMethod) handler).getMethod();
        var verify = authHandler.verify(webRequest, method);
        if (verify.isFailed()) {
            return onFailed(verify, response);
        }
        return true;
    }

    private boolean onFailed(IResponse<?> result, HttpServletResponse response) throws IOException {
        log.debug("Authentication failed with code {}: {}", result.getCode(), result.getMessage());
        response.setStatus(result.getStatus());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JacksonUtils.toJsonString(result));
        return false;
    }
}
