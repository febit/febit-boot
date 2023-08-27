package org.febit.boot.common.permission;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public interface MethodPermissionResolver {

    void collect(Method method, Consumer<PermissionItem> consumer);

    @Nullable
    default Boolean isAnonymous(Method method) {
        return null;
    }
}
