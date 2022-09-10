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
package org.febit.jooq.codegen.spi;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;

import javax.annotation.Nullable;

public interface Naming {

    @Nullable
    default String memberField(Definition def, GeneratorStrategy.Mode mode) {
        return null;
    }

    @Nullable
    default String identifier(Definition def) {
        return null;
    }

    @Nullable
    default String getPackageName(String basePackage, Definition def, GeneratorStrategy.Mode mode) {
        return null;
    }

    @Nullable
    default String className(Definition def) {
        return null;
    }
}
