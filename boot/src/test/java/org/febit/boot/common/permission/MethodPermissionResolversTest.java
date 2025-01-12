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

import org.febit.boot.permission.AnnotatedMethodPermissionResolver;
import org.febit.boot.permission.AnonymousApi;
import org.febit.boot.permission.MethodPermissionResolver;
import org.febit.boot.permission.MethodPermissionResolvers;
import org.febit.boot.permission.Permission;
import org.febit.boot.permission.PermissionItem;
import org.febit.boot.permission.ResolvedPermission;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.boot.permission.ResolvedPermission.ANONYMOUS;
import static org.febit.boot.permission.ResolvedPermission.FORBIDDEN_ABSENT;
import static org.junit.jupiter.api.Assertions.*;

class MethodPermissionResolversTest {

    private static final List<MethodPermissionResolver> resolvers = List.of(
            new AnnotatedMethodPermissionResolver()
    );

    private static ResolvedPermission resolve(Class<?> cls, String method) {
        try {
            return MethodPermissionResolvers.resolve(resolvers, cls.getDeclaredMethod(method));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void resolve() {
        ResolvedPermission permission;

        permission = resolve(String.class, "length");
        assertSame(FORBIDDEN_ABSENT, permission);

        permission = resolve(Handler.class, "ping");
        assertSame(ANONYMOUS, permission);

        permission = resolve(Handler.class, "login");
        assertSame(ANONYMOUS, permission);

        permission = resolve(Handler.class, "missingPermission");
        assertSame(FORBIDDEN_ABSENT, permission);

        permission = resolve(Handler.class, "notAnonymous");
        assertTrue(permission.isAllowList());
        assertThat(permission.getItems())
                .hasSize(1)
                .contains(PermissionItem.of("api.foo"));

        permission = resolve(Handler.class, "foo");
        assertTrue(permission.isAllowList());
        assertThat(permission.getItems())
                .hasSize(1)
                .contains(PermissionItem.of("api.foo"));

        permission = resolve(Handler.class, "bar");
        assertTrue(permission.isAllowList());
        assertThat(permission.getItems())
                .hasSize(1)
                .contains(PermissionItem.of("api:bar"));

        permission = resolve(Handler.class, "qux");
        assertTrue(permission.isAllowList());
        assertThat(permission.getItems())
                .hasSize(3)
                .contains(PermissionItem.of("api.foo"))
                .contains(PermissionItem.of("api.baz:qux"))
                .contains(PermissionItem.of("api:bar"));
    }

    @SuppressWarnings({"unused"})
    static class Handler {

        public void missingPermission() {
        }

        @AnonymousApi
        public void login() {
        }

        @Permissions.Foo
        @AnonymousApi
        public void ping() {
        }

        @AnonymousApi(false)
        @Permissions.Foo
        public void notAnonymous() {
        }

        @Permissions.Foo
        public void foo() {
        }

        @Permissions.Bar
        public void bar() {
        }

        @Permissions.Baz("qux")
        @Permissions.Foo
        @Permissions.Bar
        public void qux() {
        }
    }

    @interface Permissions {

        @Permission(code = "api.foo")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Foo {
        }

        @Permission(code = "api.baz")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Baz {

            @AliasFor(annotation = Permission.class, attribute = "action")
            String value();
        }

        @Permission(module = "api", code = "bar")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Bar {
        }
    }

}
