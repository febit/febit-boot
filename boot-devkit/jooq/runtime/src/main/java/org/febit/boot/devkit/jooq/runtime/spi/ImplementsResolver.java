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

import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;

import java.util.function.Consumer;

public interface ImplementsResolver {

    void resolve(Context context);

    interface Context extends SpiContext.WithStrategy, SpiContext.GeneratorMode {

        void addImpl(String impl);

        /**
         * @deprecated use {@link #def()} instead
         */
        @Deprecated(since = "3.4.1")
        default Definition getDef() {
            return def();
        }

        /**
         * @deprecated use {@link #mode()} instead
         */
        @Deprecated(since = "3.4.1")
        default GeneratorStrategy.Mode getMode() {
            return mode();
        }
    }

    record ContextImpl(
            JooqGeneratorStrategy strategy,
            Definition def,
            GeneratorStrategy.Mode mode,
            Consumer<String> sink
    ) implements Context {
        @Override
        public void addImpl(String impl) {
            sink.accept(impl);
        }
    }
}
