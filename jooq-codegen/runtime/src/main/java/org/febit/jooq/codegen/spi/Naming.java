package org.febit.jooq.codegen.spi;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;

import javax.annotation.Nullable;

public interface Naming {

    @Nullable
    default String getJavaMemberName(Definition def, GeneratorStrategy.Mode mode) {
        return null;
    }

    @Nullable
    default String getIdentifier(Definition def) {
        return null;
    }

    @Nullable
    default String getPackageName(String basePackage, Definition def, GeneratorStrategy.Mode mode) {
        return null;
    }

    @Nullable
    default String getClassName(Definition def, GeneratorStrategy.Mode mode) {
        return null;
    }
}
