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
