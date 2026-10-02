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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotatedMethodPermissionResolverTest {

    private final AnnotatedMethodPermissionResolver resolver = new AnnotatedMethodPermissionResolver();

    @Test
    void collectGathersAllMergedPermissions() {
        var items = new ArrayList<PermissionItem>();
        resolver.collect(method(Handler.class, "qux"), items::add);
        assertThat(items).extracting(PermissionItem::code)
                .containsExactlyInAnyOrder("api.foo", "api:bar", "api.baz:qux");
    }

    @Test
    void isAnonymousResolvesTrueForAnnotatedMethod() {
        assertThat(resolver.isAnonymous(method(Handler.class, "anonymous"))).isTrue();
    }

    @Test
    void isAnonymousResolvesFalseForExplicitFalse() {
        assertThat(resolver.isAnonymous(method(Handler.class, "notAnonymous"))).isFalse();
    }

    @Test
    void isAnonymousResolvesTrueForAnnotatedClass() {
        assertThat(resolver.isAnonymous(method(AnonymousClass.class, "onAnonymousClass"))).isTrue();
    }

    @Test
    void isAnonymousResolvesNullForNeutralMethod() {
        assertThat(resolver.isAnonymous(method(Handler.class, "neutral"))).isNull();
    }

    private static Method method(Class<?> cls, String name) {
        try {
            return cls.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    static class Handler {
        public void neutral() {
        }

        @AnonymousApi
        public void anonymous() {
        }

        @AnonymousApi(false)
        public void notAnonymous() {
        }

        @Permissions.Foo
        @Permissions.Bar
        @Permissions.Baz("qux")
        public void qux() {
        }
    }

    @AnonymousApi
    @SuppressWarnings("unused")
    static class AnonymousClass {
        public void onAnonymousClass() {
        }
    }

    @interface Permissions {

        @Permission(resource = "api.foo")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Foo {
        }

        @Permission(module = "api", resource = "bar")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Bar {
        }

        @Permission(resource = "api.baz")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface Baz {

            @AliasFor(annotation = Permission.class, attribute = "action")
            String value();
        }
    }
}
