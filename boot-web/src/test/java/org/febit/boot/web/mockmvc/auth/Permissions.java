package org.febit.boot.web.mockmvc.auth;

import org.febit.boot.common.permission.Permission;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

public @interface Permissions {

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
