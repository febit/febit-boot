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

import org.febit.jooq.codegen.JooqGeneratorStrategy;
import org.febit.jooq.codegen.meta.MetaUtils;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.TableDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class BasicImplementsResolver implements ImplementsResolver {

    @Override
    public void resolve(Context context) {
        if (!context.isTableDefinition()) {
            return;
        }

        var table = (TableDefinition) context.getDef();
        var pkCol = JooqGeneratorStrategy.getPkColumn(table);

        if (pkCol == null) {
            return;
        }

        var pkType = context.getStrategy().resolveColumnType(pkCol);
        var recordClassName = context.getStrategy()
                .getJavaClassName(table, GeneratorStrategy.Mode.RECORD);

        switch (context.getMode()) {
            case DEFAULT:
                context.addImpl(MetaUtils.CORE_PKG + ".ITable<" + recordClassName + ", " + pkType + ">");
                break;
            case POJO:
                context.addImpl(MetaUtils.CORE_PKG + ".IEntity<" + pkType + ">");
                break;
            default:
        }
    }

}
