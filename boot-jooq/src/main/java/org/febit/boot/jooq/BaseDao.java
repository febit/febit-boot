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
import org.febit.lang.UncheckedException;
import org.febit.lang.util.TypeParameters;
import org.jooq.Configuration;
import org.jooq.RecordMapper;
import org.jooq.Table;
import org.jooq.TableRecord;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Basic query DAO impl.
 *
 * @param <R>  Record Type
 * @param <PO> Pojo Type
 * @param <TB> Table Type
 * @see IBasicDao
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseDao<TB extends Table<R>, PO, R extends TableRecord<R>>
        implements IBasicDao<TB, PO, R> {

    protected final TB T; // NOPMD NOSONAR

    private final Configuration conf;
    private final RecordMapper<R, PO> mapper;
    private final Class<R> recordType;
    private final Class<PO> poType;
    private final Class<TB> tableType;

    @SuppressFBWarnings({
            "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"
    })
    protected BaseDao(Configuration conf) {
        this.conf = conf;

        var resolved = TypeParameters.forType(getClass());
        this.tableType = resolved.resolve(BaseDao.class, 0).get();
        this.poType = resolved.resolve(BaseDao.class, 1).get();
        this.recordType = resolved.resolve(BaseDao.class, 2).get();

        Objects.requireNonNull(this.tableType);
        Objects.requireNonNull(this.poType);
        Objects.requireNonNull(this.recordType);

        try {
            this.T = this.tableType.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new UncheckedException("Failed to new table instance: " + this.tableType, e);
        }
        this.mapper = mapper(this.T.recordType(), this.poType);
    }

    @Override
    public TB table() {
        return this.T;
    }

    @Override
    public Configuration conf() {
        return conf;
    }

    @Override
    public RecordMapper<R, PO> mapper() {
        return mapper;
    }

    protected Class<R> recordType() {
        return recordType;
    }

    protected Class<PO> poType() {
        return poType;
    }

    protected Class<TB> tableType() {
        return tableType;
    }
}
