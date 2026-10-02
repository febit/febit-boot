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

import org.febit.boot.auth.AuthConstants;
import org.febit.boot.auth.AuthSubject;
import org.febit.boot.auth.web.WebRequestAuthSubjectResolver;
import org.febit.boot.permission.PermissionItem;
import org.febit.boot.permission.PermissionManager;
import org.febit.boot.permission.PermissionVerifier;
import org.febit.boot.permission.ResolvedPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.tabletest.junit.TableTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebAuthHandlerImplTest {

    private final PermissionManager permissionManager = mock(PermissionManager.class);
    private final PermissionVerifier<AuthSubject> permissionVerifier = mock();
    private final WebRequestAuthSubjectResolver<AuthSubject> resolver = mock();
    private final WebAuthHandlerImpl<AuthSubject> handler =
            new WebAuthHandlerImpl<>(permissionManager, permissionVerifier, resolver);
    private final WebRequest request = mock(WebRequest.class);
    private Method handlerMethod;

    @BeforeEach
    void setUp() throws Exception {
        handlerMethod = WebAuthHandlerImplTest.class.getDeclaredMethod("dummyMethod");
    }

    @SuppressWarnings("unused")
    private void dummyMethod() {
    }

    @TableTest("""
        scenario            | type        | hasAuth | isAllow | failed | code
        ignored            | IGNORED     | false   | false   | false  |
        forbidden          | FORBIDDEN   | false   | false   | true   | FORBIDDEN_NO_PERMISSION
        allow no auth      | ALLOW_LIST  | false   | false   | true   | UNAUTHORIZED
        allow denied       | ALLOW_LIST  | true    | false   | true   | FORBIDDEN_NO_PERMISSION
        allow allowed      | ALLOW_LIST  | true    | true    | false  |
        """)
    void verifies(String type, boolean hasAuth, boolean isAllow, boolean failed, String code) {
        var item = new PermissionItem("c", "m", "r", "a");
        var auth = hasAuth ? mock(AuthSubject.class) : null;
        when(resolver.resolveAuth(request)).thenReturn(Optional.ofNullable(auth));

        ResolvedPermission permission = switch (type) {
            case "IGNORED" -> ResolvedPermission.IGNORED;
            case "FORBIDDEN" -> ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE;
            case "ALLOW_LIST" -> ResolvedPermission.allow(List.of(item));
            default -> throw new IllegalStateException(type);
        };
        when(permissionManager.getPermission(handlerMethod)).thenReturn(permission);
        when(permissionVerifier.isAllow(any(), any())).thenReturn(isAllow);

        var resp = handler.verify(request, handlerMethod);

        assertThat(resp.isFailed()).isEqualTo(failed);
        if (failed) {
            assertThat(resp.getCode()).isEqualTo(code);
        } else {
            assertThat(resp.getData()).isEqualTo(auth);
        }
    }

    @Test
    void persistsResolvedAuthToRequestAttributes() {
        var auth = mock(AuthSubject.class);
        when(auth.identifier()).thenReturn("uid");
        when(resolver.resolveAuth(request)).thenReturn(Optional.of(auth));
        when(permissionManager.getPermission(handlerMethod)).thenReturn(ResolvedPermission.IGNORED);

        handler.verify(request, handlerMethod);

        verify(request).setAttribute(AuthConstants.ATTR_AUTH, auth, RequestAttributes.SCOPE_REQUEST);
        verify(request).setAttribute(AuthConstants.ATTR_AUTH_ID, "uid", RequestAttributes.SCOPE_REQUEST);
    }

    @Test
    void clearsResolvedAuthAttributesWhenAbsent() {
        when(resolver.resolveAuth(request)).thenReturn(Optional.empty());
        when(permissionManager.getPermission(handlerMethod)).thenReturn(ResolvedPermission.IGNORED);

        handler.verify(request, handlerMethod);

        verify(request).removeAttribute(AuthConstants.ATTR_AUTH, RequestAttributes.SCOPE_REQUEST);
        verify(request).removeAttribute(AuthConstants.ATTR_AUTH_ID, RequestAttributes.SCOPE_REQUEST);
    }

    @Test
    void verifyForbiddenResponseCarriesPermissionMessage() {
        when(resolver.resolveAuth(request)).thenReturn(Optional.of(mock(AuthSubject.class)));
        when(permissionManager.getPermission(handlerMethod))
                .thenReturn(ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE);

        var resp = handler.verify(request, handlerMethod);

        assertThat(resp.isFailed()).isTrue();
        assertThat(resp.getStatus()).isEqualTo(403);
        assertThat(resp.getCode()).isEqualTo("FORBIDDEN_NO_PERMISSION");
        assertThat(resp.getMessage()).isEqualTo("Forbidden: not in charge.");
    }
}
