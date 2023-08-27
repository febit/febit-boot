package org.febit.boot.common.permission;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    String code();

    String module() default "";

    String action() default "";

    String separator() default ":";
}
