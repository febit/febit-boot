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
package org.febit.jooq.codegen;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "of")
class Spi<S> {

    private final List<S> spies;

    static <S> Spi<S> load(Class<S> type, JooqGeneratorStrategy strategy) {
        return Spi.of(
                SpiUtils.load(type, strategy)
        );
    }

    public <T> T compute(Function<S, T> computer) {
        return compute(computer, () -> null);
    }

    public <T> T compute(Function<S, T> computer, Supplier<T> defaultSupplier) {
        for (var f : spies) {
            var result = computer.apply(f);
            if (result != null) {
                return result;
            }
        }
        return defaultSupplier.get();
    }
}
