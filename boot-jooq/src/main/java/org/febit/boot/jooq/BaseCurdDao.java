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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jooq.Configuration;
import org.jooq.UpdatableRecord;
import org.springframework.core.ResolvableType;

import java.util.Objects;

/**
 * Base CURD DAO impl.
 *
 * @param <R>  Record Type
 * @param <PO> Persistent Object Type
 * @param <TB> Table Type
 * @param <ID> ID Field Type
 * @see ICurdDao
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCurdDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends BaseDao<TB, PO, R> implements ICurdDao<TB, PO, ID, R> {

    private final Class<ID> pkType;

    @SuppressWarnings({"unchecked"})
    @SuppressFBWarnings({
            "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"
    })
    protected BaseCurdDao(Configuration conf) {
        super(conf);
        this.pkType = (Class<ID>) ResolvableType.forClass(BaseCurdDao.class, getClass())
                .resolveGeneric(2);
        Objects.requireNonNull(this.pkType);
    }

    protected Class<ID> pkType() {
        return pkType;
    }

}
