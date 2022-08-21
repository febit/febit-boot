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

import org.febit.util.Lists;
import org.jooq.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * Standard dao interface.
 */
@SuppressWarnings({"unused"})
public interface ICurdDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends IBasicDao<TB, PO, R> {

    @Nullable
    default PO findById(ID id) {
        return findBy(table().pkField(), id);
    }

    @SuppressWarnings("unchecked")
    default List<PO> listByIds(ID... ids) {
        return listBy(table().pkField().in(ids));
    }

    default List<PO> listByIds(Collection<ID> ids) {
        return listBy(table().pkField().in(ids));
    }

    default boolean existsById(ID id) {
        return existsBy(table().pkField().eq(id));
    }

    default int delete(PO po) {
        return deleteById(po.id());
    }

    default int deleteById(ID id) {
        return deleteBy(table().pkField().eq(id));
    }

    default int deleteBy(Condition... conditions) {
        return dsl().deleteFrom(table())
                .where(conditions)
                .execute();
    }

    default int delete(Collection<PO> pos) {
        return deleteByIds(
                Lists.collect(pos, IEntity::id)
        );
    }

    default int deleteByIds(Collection<ID> ids) {
        return deleteBy(table().pkField().in(ids));
    }

    @SuppressWarnings("unchecked")
    default int delete(PO... pos) {
        return deleteByIds(
                Lists.collect(pos, IEntity::id)
        );
    }

    @SuppressWarnings("unchecked")
    default int deleteByIds(ID... ids) {
        return deleteByIds(asList(ids));
    }

    default InsertSetStep<R> insert() {
        return dsl().insertInto(table());
    }

    default void insert(PO po) {
        Utils.record(conf(), table(), po, false)
                .insert();
    }

    @SuppressWarnings("unchecked")
    default void insert(PO... pos) {
        insert(asList(pos));
    }

    default void insert(Collection<PO> pos) {
        if (pos.isEmpty()) {
            return;
        }
        var records = Utils.records(conf(), table(), pos, false);
        if (pos.size() == 1) {
            records.get(0).insert();
            return;
        }
        if (Utils.isNotReturnRecordToPojo(conf())) {
            dsl().batchInsert(records).execute();
            return;
        }
        for (R record : records) {
            record.insert();
        }
    }

    default int update(PO po) {
        return Utils.record(conf(), table(), po, true)
                .update();
    }

    @SuppressWarnings("unchecked")
    default int update(PO... objects) {
        return update(asList(objects));
    }

    default int update(Collection<PO> pos) {
        if (pos.isEmpty()) {
            return 0;
        }
        var records = Utils.records(conf(), table(), pos, true);
        if (pos.size() == 1) {
            return records.get(0).update();
        }
        if (Utils.isNotReturnRecordToPojo(conf())
                || Utils.isNotReturnAllOnUpdatableRecord(conf())) {
            var changes = dsl().batchUpdate(records).execute();
            return IntStream.of(changes).sum();
        }
        var changed = 0;
        for (R record : records) {
            changed += record.update();
        }
        return changed;
    }

    default <V> int updateFieldBy(Field<V> field, V value, Condition... conditions) {
        return update()
                .set(field, value)
                .where(conditions)
                .execute();
    }

    default UpdateSetFirstStep<R> update() {
        return dsl().update(table());
    }

}
