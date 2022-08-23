package org.febit.devkit.gradle.codegen.jooq;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.jooq.codegen.meta.ForcedTypes;
import org.febit.jooq.codegen.meta.Schema;
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
