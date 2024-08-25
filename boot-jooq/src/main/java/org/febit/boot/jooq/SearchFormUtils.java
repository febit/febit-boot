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
package org.febit.boot.jooq;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.febit.lang.UncheckedException;
import org.febit.lang.util.Lists;
import org.febit.lang.util.TypeParameters;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.febit.boot.jooq.Utils.declaredFields;

@Slf4j
@UtilityClass
public class SearchFormUtils {

    @SuppressWarnings({"unchecked"})
    private static final ColumnEntry<Object>[] EMPTY_ENTRY_ARRAY = new ColumnEntry[0];

    private static final Map<Class<?>, ColumnEntry<Object>[]> ENTRY_CACHE
            = new ConcurrentReferenceHashMap<>(256);

    public static List<Condition> listAnnotatedConditions(DSLContext dsl, SearchForm form) {
        var conditions = new ArrayList<Condition>();
        SearchFormUtils.collectAnnotatedConditions(dsl, form, conditions::add);
        form.apply(dsl, conditions::add);
        return conditions;
    }

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
        if (entry.ignoreEmpty()
                && "".equals(value)) {
            return null;
        }
        var jooqField = entry.field();
        var ignoreCase = entry.ignoreCase();
        return switch (entry.operator()) {
            case KEYWORD -> ignoreCase
                    ? Conditions.keywordsIgnoreCase(value.toString(), entry.multiFields)
                    : Conditions.keywords(value.toString(), entry.multiFields);
            case STARTS_WITH -> ignoreCase
                    ? jooqField.startsWithIgnoreCase(value)
                    : jooqField.startsWith(value);
            case ENDS_WITH -> ignoreCase
                    ? jooqField.endsWithIgnoreCase(value)
                    : jooqField.endsWith(value);
            case CONTAINS -> ignoreCase
                    ? jooqField.containsIgnoreCase(value)
                    : jooqField.contains(value);
            case NOT_CONTAINS -> ignoreCase
                    ? jooqField.notContainsIgnoreCase(value)
                    : jooqField.notContains(value);
            case EQ -> ignoreCase
                    ? jooqField.equalIgnoreCase(value.toString())
                    : jooqField.eq(value);
            case GT -> jooqField.gt(value);
            case GE -> jooqField.ge(value);
            case LT -> jooqField.lt(value);
            case LE -> jooqField.le(value);
            case IN -> jooqField.in(castToCollection(value));
            case NOT_IN -> jooqField.notIn(castToCollection(value));
            default -> throw new UnsupportedOperationException("Unsupported action: " + entry.operator());
        };
    }

    @Nullable
    static ColumnEntry<Object> resolveEntry(Field field) {
        var anno = AnnotatedElementUtils.findMergedAnnotation(field, Column.class);

        if (anno == null ||
                anno.operator() == Column.Operator.NONE) {
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
        var names = Lists.collect(anno.names(), Utils::name);
        names.addAll(Lists.collect(anno.values(), n -> Utils.name(anno.table(), n)));

        if (anno.operator() == Column.Operator.KEYWORD
                && names.isEmpty()) {
            log.warn("Skip search KEYWORD field: {}, since 'values' & 'names' are empty.", field);
            return null;
        }

        @SuppressWarnings({"unchecked"})
        var fieldType = (Class<Object>) (anno.operator().multiValues()
                ? resolveComponentType(field)
                : field.getType());

        return new ColumnEntry<>(
                anno.operator(),
                anno.ignoreEmpty(),
                anno.ignoreCase(),
                DSL.field(name, fieldType),
                List.copyOf(Lists.collect(
                        names, n -> DSL.field(n, fieldType)
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
        var type = field.getType();
        if (Object[].class.isAssignableFrom(type)) {
            return field.getType().getComponentType();
        }
        if (Collection.class.isAssignableFrom(type)) {
            var resolved = TypeParameters.forField(field)
                    .resolve(Collection.class, 0)
                    .get();
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

    record ColumnEntry<T>(
            Column.Operator operator,
            boolean ignoreEmpty,
            boolean ignoreCase,
            org.jooq.Field<T> field,
            List<org.jooq.Field<T>> multiFields,
            Function<T, Object> getter
    ) {
    }

}
