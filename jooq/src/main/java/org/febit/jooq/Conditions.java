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

import lombok.experimental.UtilityClass;
import org.febit.annotation.NonNullApi;
import org.febit.util.Lists;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Collection;

@NonNullApi
@UtilityClass
public class Conditions {

    @SuppressWarnings({"unchecked"})
    public static <T> Condition keywords(String value, Field<T>... fields) {
        return DSL.or(Lists.collect(
                fields,
                f -> ((Field<Object>) f).contains(value)
        ));
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Condition keywords(String value, Collection<Field<T>> fields) {
        return DSL.or(Lists.collect(
                fields,
                f -> ((Field<Object>) f).contains(value)
        ));
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Condition keywordsIgnoreCase(String value, Field<T>... fields) {
        return DSL.or(Lists.collect(
                fields,
                f -> ((Field<Object>) f).containsIgnoreCase(value)
        ));
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Condition keywordsIgnoreCase(String value, Collection<Field<T>> fields) {
        return DSL.or(Lists.collect(
                fields,
                f -> ((Field<Object>) f).containsIgnoreCase(value)
        ));
    }
}
