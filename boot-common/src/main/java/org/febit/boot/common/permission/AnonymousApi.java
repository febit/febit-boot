package org.febit.boot.common.permission;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AnonymousApi {

    boolean value() default true;
}
