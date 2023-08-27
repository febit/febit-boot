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
package org.febit.boot.web.auth;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.common.auth.AuthSubject;
import org.febit.boot.common.permission.PermissionItem;
import org.febit.boot.common.permission.PermissionManager;
import org.febit.boot.common.permission.PermissionVerifier;
import org.febit.boot.common.util.AuthErrors;
import org.febit.boot.common.util.Priority;
import org.febit.lang.protocol.IResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.util.List;

import static org.febit.boot.common.auth.AuthConstants.ATTR_AUTH;
import static org.febit.boot.common.auth.AuthConstants.ATTR_AUTH_CODE;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@Component
@Order(Priority.HIGH)
@RequiredArgsConstructor
public class WebAuthHandler<T extends AuthSubject> {

    private final PermissionManager permissionManager;
    private final PermissionVerifier<T> permissionVerifier;
    private final WebRequestAuthSubjectResolver<T> authSubjectResolver;

    public IResponse<AuthSubject> verify(WebRequest request, Method handler) {
        var permission = this.permissionManager.getPermission(handler);
        return switch (permission.getType()) {
            case IGNORED -> IResponse.success(null);
            case FORBIDDEN -> AuthErrors.FORBIDDEN_NO_PERMISSION
                    .response(permission.getMessage());
            case ALLOW_LIST -> verifyAllows(request, permission.getItems());
        };
    }

    private IResponse<AuthSubject> verifyAllows(WebRequest request, List<PermissionItem> allows) {
        var resolved = authSubjectResolver.resolveAuth(request);
        if (resolved.isEmpty()) {
            return AuthErrors.UNAUTHORIZED
                    .response(AuthErrors.UNAUTHORIZED.getCode());
        }

        var auth = resolved.get();
        var allowed = this.permissionVerifier.isAllow(auth, allows);
        if (!allowed) {
            return AuthErrors.FORBIDDEN_NO_PERMISSION
                    .response(AuthErrors.FORBIDDEN_NO_PERMISSION.getCode());
        }
        store(request, auth);
        return IResponse.success(auth);
    }

    private void store(WebRequest request, @Nullable AuthSubject auth) {
        if (auth == null) {
            request.removeAttribute(ATTR_AUTH, SCOPE_REQUEST);
            request.removeAttribute(ATTR_AUTH_CODE, SCOPE_REQUEST);
            return;
        }
        request.setAttribute(ATTR_AUTH, auth, SCOPE_REQUEST);
        request.setAttribute(ATTR_AUTH_CODE, auth.getCode(), SCOPE_REQUEST);
    }

}
