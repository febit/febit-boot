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

import jakarta.annotation.Nullable;
import lombok.Setter;
import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;
import org.febit.boot.devkit.jooq.runtime.util.NamingUtils;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Setter
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultNaming implements Naming, Aware.Strategy {

    private JooqGeneratorStrategy generatorStrategy;

    @Nullable
    @Override
    public String memberField(Definition def, GeneratorStrategy.Mode mode) {
        if (def instanceof ColumnDefinition col) {
            return NamingUtils.toLowerCamelCase(
                    generatorStrategy.resolveOutputName(col)
            );
        }
        return null;
    }

    @Nullable
    @Override
    public String identifier(Definition def) {
        if (def instanceof TableDefinition
                || def instanceof ColumnDefinition) {
            return generatorStrategy.resolveOutputName(def).toUpperCase();
        }
        return null;
    }

}
