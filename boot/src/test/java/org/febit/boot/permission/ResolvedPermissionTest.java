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
package org.febit.boot.permission;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.febit.boot.permission.ResolvedPermission.ANONYMOUS;
import static org.febit.boot.permission.ResolvedPermission.FORBIDDEN_ABSENT;
import static org.febit.boot.permission.ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE;
import static org.febit.boot.permission.ResolvedPermission.IGNORED;
import static org.febit.boot.permission.ResolvedPermission.Type;
import static org.junit.jupiter.api.Assertions.*;

class ResolvedPermissionTest {

    @Test
    void anonymousIsIgnoredBuiltIn() {
        assertBuiltIn(ANONYMOUS, true, false, false, Type.IGNORED);
    }

    @Test
    void ignoredIsIgnoredBuiltIn() {
        assertBuiltIn(IGNORED, true, false, false, Type.IGNORED);
    }

    @Test
    void forbiddenAbsentIsForbiddenBuiltIn() {
        assertBuiltIn(FORBIDDEN_ABSENT, false, true, false, Type.FORBIDDEN);
    }

    @Test
    void forbiddenNotInChargeIsForbiddenBuiltIn() {
        assertBuiltIn(FORBIDDEN_NOT_IN_CHARGE, false, true, false, Type.FORBIDDEN);
    }

    private static void assertBuiltIn(ResolvedPermission permission, boolean ignored, boolean forbidden,
                                      boolean allowList, Type type) {
        assertEquals(ignored, permission.isIgnored());
        assertEquals(forbidden, permission.isForbidden());
        assertEquals(allowList, permission.isAllowList());
        assertTrue(permission.getItems().isEmpty());
        assertEquals(type, permission.getType());
    }

    @Test
    void allowWithSingleItem() {
        var item = new PermissionItem("c", "m", "r", "a");
        var resolved = ResolvedPermission.allow(List.of(item));

        assertTrue(resolved.isAllowList());
        assertFalse(resolved.isIgnored());
        assertFalse(resolved.isForbidden());
        assertEquals(Type.ALLOW_LIST, resolved.getType());
        assertEquals(List.of(item), resolved.getItems());
        assertEquals(1, resolved.getItemsSize());
        assertEquals("Allow permissions", resolved.getMessage());
    }

    @Test
    void allowSortsItems() {
        var a = new PermissionItem("c", "m", "r", "b");
        var b = new PermissionItem("c", "m", "r", "a");
        var resolved = ResolvedPermission.allow(List.of(a, b));

        assertEquals(List.of(b, a), resolved.getItems());
    }

    @Test
    void allowRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ResolvedPermission.allow(List.of()));
    }
}
