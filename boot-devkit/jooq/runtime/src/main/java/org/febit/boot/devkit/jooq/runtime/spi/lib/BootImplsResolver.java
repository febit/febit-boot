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
package org.febit.boot.devkit.jooq.runtime.spi.lib;

import org.febit.boot.devkit.jooq.runtime.spi.ImplementsResolver;
import org.febit.boot.devkit.jooq.runtime.util.ColumnUtils;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.TableDefinition;

public class BootImplsResolver implements ImplementsResolver {

    private static final String CLS_NAMED = "org.febit.boot.common.model.INamed";
    private static final String CLS_AUDITABLE = "org.febit.boot.common.model.IAuditable";

    @Override
    public void resolve(Context context) {
        if (!context.isTableDefinition()) {
            return;
        }
        if (context.getMode() != GeneratorStrategy.Mode.POJO) {
            return;
        }
        var table = (TableDefinition) context.getDef();
        namedModel(table, context);
        auditableModel(table, context);
    }

    private void namedModel(TableDefinition table, Context context) {
        if (ColumnUtils.isString(table, "name")) {
            context.addImpl(CLS_NAMED);
        }
    }

    private void auditableModel(TableDefinition table, Context context) {
        if (ColumnUtils.isInstant(table, "created_at")
                && ColumnUtils.isInstant(table, "updated_at")
                && ColumnUtils.isString(table, "created_by")
                && ColumnUtils.isString(table, "updated_by")
        ) {
            context.addImpl(CLS_AUDITABLE);
        }
    }

}
