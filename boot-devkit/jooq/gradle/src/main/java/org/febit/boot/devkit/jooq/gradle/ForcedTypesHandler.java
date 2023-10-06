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
package org.febit.boot.devkit.jooq.gradle;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.febit.boot.devkit.jooq.meta.ForcedTypes;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.lang.modeler.Schema;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectList;
import org.gradle.internal.reflect.DirectInstantiator;
import org.jooq.meta.jaxb.ForcedType;

import java.util.Map;

public class ForcedTypesHandler extends DefaultNamedDomainObjectList<ForcedType> {

    public ForcedTypesHandler() {
        super(ForcedType.class, DirectInstantiator.INSTANCE, ForcedTypesHandler::getForcedTypeId,
                CollectionCallbackActionDecorator.NOOP);
    }

    private static String getForcedTypeId(ForcedType type) {
        return type.getIncludeTypes()
                + '/' + type.getIncludeExpression();
    }

    public void add(@DelegatesTo(ForcedType.class) Closure<?> closure) {
        add(GradleUtils.to(closure, new ForcedType()));
    }

    public void mapping(Map<Object, Map<Object, Object>> map) {
        map.forEach((table, pair) -> pair.forEach((col, type) -> {
            var expr = column(table.toString(), col.toString());
            var schema = Schema.parse(type.toString());
            ForcedTypes.to(this::add, expr, schema);
        }));
    }

    private static String column(String table, String col) {
        return ".*\\." + table + "\\." + col;
    }

    public void timeToInstant() {
        ForcedTypes.timeToInstant(this::add);
    }
}
