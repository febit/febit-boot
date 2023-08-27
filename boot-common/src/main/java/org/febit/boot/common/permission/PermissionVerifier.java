package org.febit.boot.common.permission;

import org.febit.boot.common.auth.AuthSubject;

import java.util.Collection;

public interface PermissionVerifier<T extends AuthSubject> {

    boolean isAllow(T auth, Collection<PermissionItem> permissions);
}
