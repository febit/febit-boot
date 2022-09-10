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
package org.febit.jooq.codegen.spi.lib;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.jooq.codegen.spi.TableFilter;
import org.jooq.meta.TableDefinition;

@Slf4j
public class FlywayExcludeFilter implements TableFilter {

    @Override
    public boolean exclude(TableDefinition def) {
        if (StringUtils.startsWithIgnoreCase(def.getName(),
                "flyway_")) {
            log.info("Excluded flyway table: {}", def.getName());
            return true;
        }
        return false;
    }
}
