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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.febit.lang.util.Streams;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@UtilityClass
public class MethodPermissionResolvers {

    public static <R extends MethodPermissionResolver> List<PermissionItem> resolveItems(
            Iterable<R> resolvers, Method method) {
        return Streams.of(resolvers)
                .<PermissionItem>mapMulti((resolver, sink) ->
                        resolver.collect(method, sink)
                )
                .distinct()
                .sorted()
                .toList();
    }

    public static <R extends MethodPermissionResolver> ResolvedPermission resolve(
            Iterable<R> resolvers, Method method) {
        if (isAnonymous(resolvers, method)) {
            return ResolvedPermission.ANONYMOUS;
        }

        var entries = resolveItems(resolvers, method);
        if (!entries.isEmpty()) {
            return ResolvedPermission.allow(entries);
        }
        log.warn(
                "Missing permission: {}#{}(...)",
                method.getDeclaringClass().getName(),
                method.getName()
        );
        return ResolvedPermission.FORBIDDEN_ABSENT;
    }

    public static <R extends MethodPermissionResolver> boolean isAnonymous(
            Iterable<R> resolvers, Method method) {
        Boolean anonymousOrAbsent = null;
        for (R resolver : resolvers) {
            var result = resolver.isAnonymous(method);
            if (result == null) {
                continue;
            }
            if (!result) {
                return false;
            }
            anonymousOrAbsent = true;
        }
        return anonymousOrAbsent != null;
    }

}
