package org.febit.boot.common.permission;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.boot.common.permission.ResolvedPermission.ANONYMOUS;
import static org.febit.boot.common.permission.ResolvedPermission.FORBIDDEN_ABSENT;
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
