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
package org.febit.boot.devkit.jooq.runtime.spi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

import java.util.function.Consumer;

public interface ImplementsResolver {

    void resolve(Context context);

    interface Context extends SpiContext {

        void addImpl(String impl);

        JooqGeneratorStrategy getStrategy();

        Definition getDef();

        GeneratorStrategy.Mode getMode();

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        default boolean isTableDefinition() {
            return getDef() instanceof TableDefinition;
        }
    }

    @Getter
    @RequiredArgsConstructor
    class ContextImpl implements Context {
        private final JooqGeneratorStrategy strategy;
        private final Definition def;
        private final GeneratorStrategy.Mode mode;
        private final Consumer<String> consumer;

        @Override
        public void addImpl(String impl) {
            consumer.accept(impl);
        }

    }
}
