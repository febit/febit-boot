package org.febit.jooq.codegen.spi.lib;

import org.apache.commons.lang3.StringUtils;
import org.febit.jooq.codegen.spi.DatabaseFilter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

@Slf4j
public class FlywayExcludeFilter implements DatabaseFilter {

    @Override
    public boolean exclude(Definition def) {
        if ((def instanceof TableDefinition)
                && StringUtils.startsWithIgnoreCase(def.getName(),
                "flyway_")) {
            log.debug("Excluded flyway table: {}", def.getName());
            return true;
        }
        return false;
    }
}
