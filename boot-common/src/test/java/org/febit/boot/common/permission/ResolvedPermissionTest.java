package org.febit.boot.common.permission;

import org.junit.jupiter.api.Test;

import static org.febit.boot.common.permission.ResolvedPermission.ANONYMOUS;
import static org.febit.boot.common.permission.ResolvedPermission.FORBIDDEN_ABSENT;
import static org.febit.boot.common.permission.ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE;
import static org.febit.boot.common.permission.ResolvedPermission.IGNORED;
import static org.febit.boot.common.permission.ResolvedPermission.Type;
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
