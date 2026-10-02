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
package org.febit.boot.permission;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

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
    void resolveReturnsForbiddenAbsentForUnannotatedStringLength() {
        assertSame(FORBIDDEN_ABSENT, resolve(String.class, "length"));
    }

    @Test
    void resolveReturnsAnonymousForLogin() {
        assertSame(ANONYMOUS, resolve(Handler.class, "login"));
    }

    @Test
    void resolveReturnsAnonymousForPing() {
        assertSame(ANONYMOUS, resolve(Handler.class, "ping"));
    }

    @Test
    void resolveReturnsForbiddenAbsentForMissingPermission() {
        assertSame(FORBIDDEN_ABSENT, resolve(Handler.class, "missingPermission"));
    }

    @Test
    void resolveCollectsAllowListForNotAnonymous() {
        assertAllowList("notAnonymous", List.of("api.foo"));
    }

    @Test
    void resolveCollectsAllowListForFoo() {
        assertAllowList("foo", List.of("api.foo"));
    }

    @Test
    void resolveCollectsAllowListForBar() {
        assertAllowList("bar", List.of("api:bar"));
    }

    @Test
    void resolveCollectsAllowListForQux() {
        assertAllowList("qux", List.of("api.foo", "api.baz:qux", "api:bar"));
    }

    private void assertAllowList(String method, List<String> expectedCodes) {
        var permission = resolve(Handler.class, method);
        assertTrue(permission.isAllowList());
        assertThat(permission.getItems())
                .map(PermissionItem::code)
                .containsExactlyInAnyOrderElementsOf(expectedCodes);
    }

    @Test
    void isAnonymousResolvesPerResolverBranches() throws Exception {
        var method = Handler.class.getDeclaredMethod("missingPermission");
        assertThat(MethodPermissionResolvers.isAnonymous(List.of(new StubResolver(null)), method)).isFalse();
        assertThat(MethodPermissionResolvers.isAnonymous(List.of(new StubResolver(false)), method)).isFalse();
        assertThat(MethodPermissionResolvers.isAnonymous(List.of(new StubResolver(true)), method)).isTrue();
        assertThat(MethodPermissionResolvers.isAnonymous(
                List.of(new StubResolver(null), new StubResolver(true)), method)).isTrue();
        assertThat(MethodPermissionResolvers.isAnonymous(
                List.of(new StubResolver(true), new StubResolver(false)), method)).isFalse();
    }

    static class StubResolver implements MethodPermissionResolver {
        private final Boolean anonymous;

        StubResolver(Boolean anonymous) {
            this.anonymous = anonymous;
        }

        @Override
        public void collect(Method method, Consumer<PermissionItem> consumer) {
        }

        @Override
        public Boolean isAnonymous(Method method) {
            return anonymous;
        }
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

        @Permission(resource = "api.foo")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Foo {
        }

        @Permission(resource = "api.baz")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Baz {

            @AliasFor(annotation = Permission.class, attribute = "action")
            String value();
        }

        @Permission(module = "api", resource = "bar")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Bar {
        }
    }

}
