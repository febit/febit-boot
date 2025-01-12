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
package org.febit.boot.common.permission;

import org.febit.boot.permission.ResolvedPermission;
import org.junit.jupiter.api.Test;

import static org.febit.boot.permission.ResolvedPermission.ANONYMOUS;
import static org.febit.boot.permission.ResolvedPermission.FORBIDDEN_ABSENT;
import static org.febit.boot.permission.ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE;
import static org.febit.boot.permission.ResolvedPermission.IGNORED;
import static org.febit.boot.permission.ResolvedPermission.Type;
import static org.junit.jupiter.api.Assertions.*;

class ResolvedPermissionTest {

    @Test
    void buildIn() {
        assertTrue(ANONYMOUS.isIgnored());
        assertFalse(ANONYMOUS.isForbidden());
        assertTrue(ANONYMOUS.getItems().isEmpty());
        assertEquals(ResolvedPermission.Type.IGNORED, ANONYMOUS.getType());

        assertTrue(IGNORED.isIgnored());
        assertFalse(IGNORED.isForbidden());
        assertTrue(IGNORED.getItems().isEmpty());
        assertEquals(ResolvedPermission.Type.IGNORED, IGNORED.getType());

        assertFalse(FORBIDDEN_ABSENT.isIgnored());
        assertTrue(FORBIDDEN_ABSENT.isForbidden());
        assertTrue(FORBIDDEN_ABSENT.getItems().isEmpty());
        assertEquals(Type.FORBIDDEN, FORBIDDEN_ABSENT.getType());

        assertFalse(FORBIDDEN_NOT_IN_CHARGE.isIgnored());
        assertTrue(FORBIDDEN_NOT_IN_CHARGE.isForbidden());
        assertTrue(FORBIDDEN_NOT_IN_CHARGE.getItems().isEmpty());
        assertEquals(Type.FORBIDDEN, FORBIDDEN_NOT_IN_CHARGE.getType());
    }
}
