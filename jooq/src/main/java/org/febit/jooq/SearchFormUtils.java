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
package org.febit.jooq;

import jodd.exception.UncheckedException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.febit.util.Lists;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.annotation.Nullable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.febit.jooq.Utils.declaredFields;

@Slf4j
@UtilityClass
public class SearchFormUtils {

    @SuppressWarnings({"unchecked"})
    private static final ColumnEntry<Object>[] EMPTY_ENTRY_ARRAY = new ColumnEntry[0];

    private static final Map<Class<?>, ColumnEntry<Object>[]> ENTRY_CACHE
            = new ConcurrentReferenceHashMap<>(256);

    public static void collectAnnotatedConditions(DSLContext dsl, SearchForm form, Consumer<Condition> consumer) {
        var configs = ENTRY_CACHE.computeIfAbsent(form.getClass(), SearchFormUtils::resolveEntries);
        for (var config : configs) {
            var value = config.getter.apply(form);
            var condition = resolveCondition(config, value);
            if (condition == null) {
                continue;
            }
            consumer.accept(condition);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static ColumnEntry<Object>[] resolveEntries(Class<?> formType) {

        var configs = declaredFields(formType)
                .map(SearchFormUtils::resolveEntry)
                .filter(Objects::nonNull)
                .toArray(ColumnEntry[]::new);

        if (configs.length == 0) {
            return EMPTY_ENTRY_ARRAY;
        }
        return configs;
    }

    @Nullable
    @SuppressWarnings({"squid:MethodCyclomaticComplexity"})
    private static <T> Condition resolveCondition(ColumnEntry<T> entry, @Nullable T value) {
        if (value == null) {
            return null;
        }
        if (entry.isIgnoreEmpty()
                && "".equals(value)) {
            return null;
        }
        var jooqField = entry.getField();
        var ignoreCase = entry.isIgnoreCase();
        switch (entry.getOperator()) {
            case KEYWORD:
                return ignoreCase
                        ? Conditions.keywordsIgnoreCase(value.toString(), entry.multiFields)
                        : Conditions.keywords(value.toString(), entry.multiFields);
            case STARTS_WITH:
                return ignoreCase
                        ? jooqField.startsWithIgnoreCase(value)
                        : jooqField.startsWith(value);
            case ENDS_WITH:
                return ignoreCase
                        ? jooqField.endsWithIgnoreCase(value)
                        : jooqField.endsWith(value);
            case CONTAINS:
                return ignoreCase
                        ? jooqField.containsIgnoreCase(value)
                        : jooqField.contains(value);
            case NOT_CONTAINS:
                return ignoreCase
                        ? jooqField.notContainsIgnoreCase(value)
                        : jooqField.notContains(value);
            case EQ:
                return ignoreCase
                        ? jooqField.equalIgnoreCase(value.toString())
                        : jooqField.eq(value);
            case GT:
                return jooqField.gt(value);
            case GE:
                return jooqField.ge(value);
            case LT:
                return jooqField.lt(value);
            case LE:
                return jooqField.le(value);
            case IN:
                return jooqField.in(castToCollection(value));
            case NOT_IN:
                return jooqField.notIn(castToCollection(value));
            default:
                throw new UnsupportedOperationException("Unsupported action: " + entry.getOperator());
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked"})
    public static ColumnEntry<Object> resolveEntry(Field field) {
        var anno = AnnotatedElementUtils.findMergedAnnotation(field, Column.class);

        if (anno == null ||
                anno.operator() == Column.Operator.NONE) {
            return null;
        }

        if (anno.operator() == Column.Operator.KEYWORD
                && anno.names().length == 0) {
            log.warn("Skip search KEYWORD field: {}, since 'names' is empty.", field);
            return null;
        }

        if (anno.operator().multiValues()
                && !isSupportedCollection(field)
        ) {
            throw new UnsupportedOperationException("Unsupported type for '"
                    + anno.operator()
                    + "', only collections and object arrays are accepted: "
                    + field);
        }

        var name = Utils.name(anno, field.getName());
        var fieldType = anno.operator().multiValues()
                ? resolveComponentType(field)
                : field.getType();

        return new ColumnEntry(
                anno.operator(),
                anno.ignoreEmpty(),
                anno.ignoreCase(),
                DSL.field(name, fieldType),
                List.copyOf(Lists.collect(
                        anno.names(),
                        n -> DSL.field(Utils.name(n), fieldType)
                )),
                getter(field)
        );
    }

    @SuppressWarnings({"unchecked"})
    private static Collection<Object> castToCollection(@Nullable Object value) {
        if (value instanceof Collection) {
            return (Collection<Object>) value;
        }
        if (value instanceof Object[]) {
            return Lists.collect((Object[]) value);
        }
        if (value instanceof Iterable) {
            return Lists.collect((Iterable<Object>) value);
        }
        if (value == null) {
            return List.of();
        }
        throw new IllegalArgumentException("Value cannot cast to collection: " + value.getClass());
    }

    private static boolean isSupportedCollection(Field field) {
        return Object[].class.isAssignableFrom(field.getType())
                || Collection.class.isAssignableFrom(field.getType());
    }

    static Class<?> resolveComponentType(Field field) {
        if (Object[].class.isAssignableFrom(field.getType())) {
            return field.getType().getComponentType();
        }
        if (Collection.class.isAssignableFrom(field.getType())) {
            var resolved = ResolvableType.forField(field).asCollection()
                    .resolveGeneric(0);
            return resolved != null ? resolved : Object.class;
        }
        throw new IllegalArgumentException("Field type not supported, collection types are accepted: " + field);
    }

    static <T> Function<T, Object> getter(Field field) {
        setAccessible(field);
        return bean -> {
            try {
                return field.get(bean);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(e);
            }
        };
    }

    private static void setAccessible(AccessibleObject obj) {
        try {
            obj.setAccessible(true);
        } catch (final SecurityException e) {
            log.debug("Ignored SecurityException", e);
        }
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    static class ColumnEntry<T> {
        private final Column.Operator operator;
        private final boolean ignoreEmpty;
        private final boolean ignoreCase;
        private final org.jooq.Field<T> field;
        private final List<org.jooq.Field<T>> multiFields;
        private final Function<T, Object> getter;
    }

}
