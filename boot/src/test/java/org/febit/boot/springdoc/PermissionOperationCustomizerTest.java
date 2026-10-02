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
package org.febit.boot.springdoc;

import io.swagger.v3.oas.models.Operation;
import org.febit.boot.permission.PermissionItem;
import org.febit.boot.permission.PermissionManager;
import org.febit.boot.permission.ResolvedPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionOperationCustomizerTest {

    private static final String X_PERMISSION = PermissionOperationCustomizer.X_PERMISSION;

    @Test
    void addsXPermissionExtensionForAllowList() throws Exception {
        assertXPermissionExtension(allowItems(), ResolvedPermission.Type.ALLOW_LIST, List.of("a", "b"));
    }

    @Test
    void addsXPermissionExtensionForForbidden() throws Exception {
        assertXPermissionExtension(ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE,
                ResolvedPermission.Type.FORBIDDEN, List.of());
    }

    @Test
    void addsXPermissionExtensionForIgnored() throws Exception {
        assertXPermissionExtension(ResolvedPermission.IGNORED, ResolvedPermission.Type.IGNORED, List.of());
    }

    @Test
    void permissionDocMetaCollectsAllowListItems() {
        assertPermissionDocMeta(allowItems(), ResolvedPermission.Type.ALLOW_LIST, List.of("a", "b"));
    }

    @Test
    void permissionDocMetaCollectsForbiddenItems() {
        assertPermissionDocMeta(ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE,
                ResolvedPermission.Type.FORBIDDEN, List.of());
    }

    @Test
    void permissionDocMetaCollectsIgnoredItems() {
        assertPermissionDocMeta(ResolvedPermission.IGNORED, ResolvedPermission.Type.IGNORED, List.of());
    }

    private static ResolvedPermission allowItems() {
        var items = List.of(
                new PermissionItem("a", "m", "r", "act"),
                new PermissionItem("b", "m", "r", "act")
        );
        return ResolvedPermission.allow(items);
    }

    private void assertXPermissionExtension(ResolvedPermission resolved,
                                            ResolvedPermission.Type expectedType,
                                            List<String> expectedItems) throws Exception {
        var manager = mock(PermissionManager.class);
        Method method = handlerMethod();
        when(manager.getPermission(method)).thenReturn(resolved);

        Operation result = new PermissionOperationCustomizer(manager)
                .customize(new Operation(), new HandlerMethod(this, method));

        var extensions = result.getExtensions();
        assertThat(extensions).containsOnlyKeys(X_PERMISSION);
        var meta = (PermissionOperationCustomizer.PermissionDocMeta) extensions.get(X_PERMISSION);
        assertThat(meta.getType()).isEqualTo(expectedType);
        assertThat(meta.getItems()).isEqualTo(expectedItems);
    }

    private static void assertPermissionDocMeta(ResolvedPermission resolved,
                                                ResolvedPermission.Type expectedType,
                                                List<String> expectedItems) {
        var meta = PermissionOperationCustomizer.PermissionDocMeta.of(resolved);
        assertThat(meta.getType()).isEqualTo(expectedType);
        assertThat(meta.getItems()).isEqualTo(expectedItems);
    }

    private static Method handlerMethod() throws NoSuchMethodException {
        return PermissionOperationCustomizerTest.class.getDeclaredMethod("handler");
    }

    @SuppressWarnings("unused")
    private void handler() {
    }
}
