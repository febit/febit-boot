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

import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.febit.lang.protocal.Sort;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.febit.jooq.Utils.declaredFields;

@Slf4j
@UtilityClass
public class SortUtils {

    private static final Map<Class<?>, Map<String, SortEntry>> CACHE
            = new ConcurrentReferenceHashMap<>(256);

    public static List<OrderField<?>> resolve(List<Sort> sorts, SearchForm form) {
        var mapper = CACHE.computeIfAbsent(form.getClass(), SortUtils::resolveFromForm);
        var result = new ArrayList<OrderField<?>>(mapper.size());
        for (var sort : sorts) {
            var entry = mapper.get(sort.getProperty());
            if (entry == null) {
                throw new IllegalArgumentException("Not support sort by property: " + sort.getProperty());
            }
            result.add(sort.isAsc()
                    ? entry.field.asc()
                    : entry.field.desc()
            );
        }
        return result;
    }

    private static Map<String, SortEntry> resolveFromForm(Class<?> formClass) {
        var orderByAnno = formClass.getAnnotation(OrderMappingBy.class);
        if (orderByAnno == null) {
            return Map.of();
        }
        return CACHE.computeIfAbsent(orderByAnno.value(), SortUtils::resolve);
    }

    private static Map<String, SortEntry> resolve(Class<?> mappingType) {
        var configs = declaredFields(mappingType)
                .map(SortUtils::resolveEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        SortEntry::getName,
                        Function.identity()
                ));

        if (configs.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(configs);
    }

    @Nullable
    public static SortEntry resolveEntry(Field field) {
        var anno = AnnotatedElementUtils.findMergedAnnotation(field, Column.class);
        var name = Utils.name(anno, field.getName());
        return new SortEntry(field.getName(), DSL.field(name, field.getType()));
    }

    @Data
    public static class SortEntry {
        private final String name;
        private final org.jooq.Field<?> field;
    }
}
