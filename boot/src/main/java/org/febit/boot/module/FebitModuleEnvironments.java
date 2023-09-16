package org.febit.boot.module;

import org.febit.lang.module.IModule;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FebitModuleEnvironments.List.class)
public @interface FebitModuleEnvironments {

    String prefix() default "app.module";

    Class<? extends IModule> value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {

        FebitModuleEnvironments[] value();
    }
}
