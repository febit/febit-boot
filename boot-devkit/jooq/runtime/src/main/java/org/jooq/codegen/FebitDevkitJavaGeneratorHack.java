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
package org.jooq.codegen;

import org.febit.lang.annotation.NonNullApi;
import org.jooq.meta.Database;

@NonNullApi
public abstract class FebitDevkitJavaGeneratorHack extends JavaGenerator {

    @Override
    void logDatabaseParameters(Database db) {
        initDatabase(db);
        super.logDatabaseParameters(db);
    }

    protected abstract void initDatabase(Database db);
}
