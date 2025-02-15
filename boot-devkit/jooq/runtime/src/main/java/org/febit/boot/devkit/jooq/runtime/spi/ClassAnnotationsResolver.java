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

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;

import java.util.function.Consumer;

public interface ClassAnnotationsResolver {

    void resolve(Context context);

    interface Context extends SpiContext.GeneratorMode {

        void out(String pattern, String... refs);

        GeneratorStrategy.Mode mode();

        default void process(ClassAnnotationsResolver resolver) {
            resolver.resolve(this);
        }
    }

    record CodePattern(
            String pattern,
            String[] refs
    ) {
    }

    record ContextImpl(
            Definition def,
            GeneratorStrategy.Mode mode,
            Consumer<CodePattern> sink
    ) implements Context {

        @Override
        public void out(String impl, String... refs) {
            sink.accept(new CodePattern(impl, refs));
        }
    }
}
