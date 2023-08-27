package org.febit.boot.common.permission;

import java.lang.reflect.Method;
import java.util.List;

public interface PermissionManager {

    ResolvedPermission getPermission(Method method);

    List<PermissionItem> getAllPermissionItems();
}
