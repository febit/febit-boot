package org.febit.boot.common.permission;

import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class AnnotatedMethodPermissionResolver implements MethodPermissionResolver {

    @Override
    public void collect(Method method, Consumer<PermissionItem> consumer) {
        AnnotatedElementUtils
                .getAllMergedAnnotations(method, Permission.class)
                .stream()
                .map(PermissionItem::of)
                .distinct()
                .forEach(consumer);
    }

    @Nullable
    @Override
    public Boolean isAnonymous(Method method) {
        var anonymous = method.getAnnotation(AnonymousApi.class);
        if (anonymous == null) {
            anonymous = method.getDeclaringClass()
                    .getAnnotation(AnonymousApi.class);
        }
        return anonymous != null
                ? anonymous.value()
                : null;
    }
}
