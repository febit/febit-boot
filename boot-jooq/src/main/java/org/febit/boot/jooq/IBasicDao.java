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

import org.apache.commons.collections4.CollectionUtils;
import org.febit.lang.protocol.Page;
import org.febit.lang.protocol.Pagination;
import org.jooq.Record;
import org.jooq.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Basic DAO interface.
 */
@SuppressWarnings({"unused"})
public interface IBasicDao<TB extends Table<R>, PO, R extends TableRecord<R>> {

    default <V> RecordMapper<R, V> mapper(Class<V> beanType) {
        return mapper(table().recordType(), beanType);
    }

    TB table();

    default <V, R1 extends Record> RecordMapper<R1, V> mapper(RecordType<R1> recordType, Class<V> beanType) {
        return conf()
                .recordMapperProvider()
                .provide(recordType, beanType);
    }

    Configuration conf();

    @Nullable
    default <Z> PO findBy(Field<Z> field, Z value) {
        return findBy(field.equal(value));
    }

    @Nullable
    default PO findBy(Condition... conditions) {
        return findBy(Arrays.asList(conditions), mapper());
    }

    RecordMapper<R, PO> mapper();

    @Nullable
    default <V> V findBy(List<Condition> conditions, RecordMapper<R, V> mapper) {
        R record = dsl().selectFrom(table())
                .where(conditions)
                .fetchOne();
        return record == null ? null : mapper.map(record);
    }

    default DSLContext dsl() {
        return conf().dsl();
    }

    @Nullable
    default <V> V findBy(RecordMapper<R, V> mapper, Condition... conditions) {
        return findBy(Arrays.asList(conditions), mapper);
    }

    @Nullable
    default PO findBy(List<Condition> conditions) {
        return findBy(conditions, mapper());
    }

    @Nullable
    default PO findBy(SearchForm form) {
        return findBy(form.toConditions(dsl()), mapper());
    }

    @Nullable
    default <V> V findBy(SearchForm form, RecordMapper<R, V> mapper) {
        return findBy(form.toConditions(dsl()), mapper);
    }

    @Nullable
    default <F> F findFieldBy(Field<F> field, Condition... conditions) {
        return dsl()
                .select(field)
                .from(table())
                .where(conditions)
                .fetchOne(field);
    }

    default List<PO> listBy(Condition... conditions) {
        return listBy(Arrays.asList(conditions), mapper());
    }

    default <V> List<V> listBy(List<Condition> conditions, RecordMapper<R, V> mapper) {
        return dsl().selectFrom(table())
                .where(conditions)
                .fetch()
                .map(mapper);
    }

    default <V> List<V> listBy(RecordMapper<R, V> mapper, Condition... conditions) {
        return listBy(Arrays.asList(conditions), mapper);
    }

    default List<PO> listBy(List<Condition> conditions) {
        return listBy(conditions, mapper());
    }

    default List<PO> listBy(SearchForm form) {
        return listBy(form.toConditions(dsl()), mapper());
    }

    default <V> List<V> listBy(SearchForm form, RecordMapper<R, V> mapper) {
        return listBy(form.toConditions(dsl()), mapper);
    }

    default List<PO> listAll() {
        return dsl().selectFrom(table())
                .fetch(mapper());
    }

    default <F> List<F> listFieldBy(Field<F> field, Condition... conditions) {
        return listFieldBy(field, Arrays.asList(conditions));
    }

    default <F> List<F> listFieldBy(Field<F> field, List<Condition> conditions) {
        return dsl()
                .select(field)
                .from(table())
                .where(conditions)
                .fetch(field);
    }

    default <F> List<F> listFieldBy(Field<F> field, SearchForm form) {
        return listFieldBy(field, form.toConditions(dsl()));
    }

    default long countBy(Condition... conditions) {
        return countBy(Arrays.asList(conditions));
    }

    default long countBy(List<Condition> conditions) {
        Long count = dsl().selectCount()
                .from(table())
                .where(conditions)
                .fetchOne(0, Long.class);
        assert count != null;
        return count;
    }

    default long countBy(SearchForm form) {
        return countBy(form.toConditions(dsl()));
    }

    default boolean existsBy(Condition... conditions) {
        return existsBy(Arrays.asList(conditions));
    }

    default boolean existsBy(List<Condition> conditions) {
        return countBy(conditions) > 0L;
    }

    default boolean existsBy(SearchForm form) {
        return existsBy(form.toConditions(dsl()));
    }

    default Page<PO> page(Pagination pagination, SearchForm form) {
        return page(pagination, form, mapper());
    }

    default <V> Page<V> page(
            Pagination pagination, SearchForm form, RecordMapper<R, V> mapper
    ) {
        var conditions = form.toConditions(dsl());
        return page(
                pagination,
                dsl().selectFrom(table())
                        .where(conditions),
                SortUtils.resolve(pagination.getSorts(), form),
                mapper
        );
    }

    default <V, R1 extends Record> Page<V> page(
            Pagination pagination,
            TableLike<R1> table,
            Collection<? extends OrderField<?>> orders,
            RecordMapper<R1, V> mapper
    ) {
        return customPage(pagination, table, orders, query -> query.fetch(mapper));
    }

    default <V, R1 extends Record> Page<V> customPage(
            Pagination pagination,
            TableLike<R1> table,
            Collection<? extends OrderField<?>> orders,
            Function<SelectQuery<R1>, List<V>> action
    ) {
        var total = dsl().selectCount()
                .from(table)
                .fetchOne(0, Long.class);
        assert total != null;

        if (total <= pagination.offset()) {
            return Page.of(pagination.getPage(), pagination.getSize(),
                    total, Collections.emptyList());
        }

        var query = dsl().selectQuery(table);
        query.addSelect(table.fields());
        query.addLimit(pagination.offset(), pagination.getSize());
        if (CollectionUtils.isNotEmpty(orders)) {
            query.addOrderBy(orders);
        }

        var rows = action.apply(query);
        return Page.of(pagination.getPage(), pagination.getSize(), total, rows);
    }
}
