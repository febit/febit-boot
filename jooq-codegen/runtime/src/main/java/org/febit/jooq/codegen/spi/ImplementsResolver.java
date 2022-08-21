package org.febit.jooq.codegen.spi;

import org.febit.jooq.codegen.JooqGeneratorStrategy;
import lombok.Data;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

import java.util.function.Consumer;

public interface ImplementsResolver {

    void resolve(Context context);

    @Data
    class Context {
        private final JooqGeneratorStrategy strategy;
        private final Definition def;
        private final GeneratorStrategy.Mode mode;
        private final Consumer<String> consumer;

        public void addImpl(String impl) {
            consumer.accept(impl);
        }

        public boolean isTableDefinition() {
            return def instanceof TableDefinition;
        }
    }
}
