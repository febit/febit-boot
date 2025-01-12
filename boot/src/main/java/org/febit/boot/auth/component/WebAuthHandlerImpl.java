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
package org.febit.boot.auth.component;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.auth.AuthSubject;
import org.febit.boot.auth.web.WebAuthHandler;
import org.febit.boot.auth.web.WebRequestAuthSubjectResolver;
import org.febit.boot.common.permission.PermissionItem;
import org.febit.boot.common.permission.PermissionManager;
import org.febit.boot.common.permission.PermissionVerifier;
import org.febit.boot.common.util.AuthErrors;
import org.febit.boot.common.util.Priority;
import org.febit.lang.protocol.IResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.util.List;

import static org.febit.boot.auth.AuthConstants.ATTR_AUTH;
import static org.febit.boot.auth.AuthConstants.ATTR_AUTH_ID;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@Component
@Order(Priority.HIGH)
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class WebAuthHandlerImpl<T extends AuthSubject> implements WebAuthHandler<T> {

    private final PermissionManager permissionManager;
    private final PermissionVerifier<T> permissionVerifier;
    private final WebRequestAuthSubjectResolver<T> authSubjectResolver;

    @Override
    public IResponse<AuthSubject> verify(WebRequest request, Method handler) {
        var resolved = authSubjectResolver.resolveAuth(request);
        store(request, resolved.orElse(null));

        var permission = this.permissionManager.getPermission(handler);
        switch (permission.getType()) {
            case IGNORED -> {
                return IResponse.success(null);
            }
            case FORBIDDEN -> {
                return AuthErrors.FORBIDDEN_NO_PERMISSION
                        .response(permission.getMessage());
            }
        }

        if (resolved.isEmpty()) {
            return AuthErrors.UNAUTHORIZED
                    .response(AuthErrors.UNAUTHORIZED.getCode());
        }

        var auth = resolved.get();
        return verifyAllows(auth, permission.getItems());
    }

    private IResponse<AuthSubject> verifyAllows(T auth, List<PermissionItem> allows) {
        var allowed = this.permissionVerifier.isAllow(auth, allows);
        if (!allowed) {
            return AuthErrors.FORBIDDEN_NO_PERMISSION
                    .response(AuthErrors.FORBIDDEN_NO_PERMISSION.getCode());
        }
        return IResponse.success(auth);
    }

    private void store(WebRequest request, @Nullable T auth) {
        if (auth == null) {
            request.removeAttribute(ATTR_AUTH, SCOPE_REQUEST);
            request.removeAttribute(ATTR_AUTH_ID, SCOPE_REQUEST);
            return;
        }
        request.setAttribute(ATTR_AUTH, auth, SCOPE_REQUEST);
        request.setAttribute(ATTR_AUTH_ID, auth.identifier(), SCOPE_REQUEST);
    }

}
