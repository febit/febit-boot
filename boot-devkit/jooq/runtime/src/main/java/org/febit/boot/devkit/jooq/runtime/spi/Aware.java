package org.febit.boot.devkit.jooq.runtime.spi;

import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;

public interface Aware {

    interface Strategy extends Aware {

        void setGeneratorStrategy(JooqGeneratorStrategy strategy);
    }
}
