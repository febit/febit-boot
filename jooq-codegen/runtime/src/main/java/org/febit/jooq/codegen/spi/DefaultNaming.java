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
package org.febit.jooq.codegen.spi;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;
import org.jooq.tools.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class DefaultNaming implements Naming {

    @Override
    public String getIdentifier(Definition def) {
        if (def instanceof TableDefinition) {
            return getOutputName(def).toUpperCase();
        }
        return null;
    }

    @Override
    public String getPackageName(String basePackage, Definition def, GeneratorStrategy.Mode mode) {
        switch (mode) {
            case RECORD:
                return basePackage + ".record";
            case POJO:
                return basePackage + ".po";
            case DAO:
                return basePackage + ".dao";
            case INTERFACE:
                return basePackage + ".interfaces";
            case DEFAULT:
                if (def instanceof TableDefinition) {
                    return basePackage + ".table";
                }
                break;
            default:
        }
        return null;
    }

    @Override
    public String getClassName(Definition def, GeneratorStrategy.Mode mode) {
        String fixedName = toCamelCase(getOutputName(def));
        return decorateClassName(fixedName, def, mode);
    }

    protected String toCamelCase(String name) {
        name = org.apache.commons.lang3.StringUtils
                .replaceChars(name, " -.", "___");

        return StringUtils.toCamelCase(name);
    }

    protected String decorateClassName(String name, Definition def, GeneratorStrategy.Mode mode) {

        switch (mode) {
            case INTERFACE:
                return "I" + name;
            case RECORD:
                return name + "Record";
            case DAO:
                return name + "Dao";
            case POJO:
                return name + "PO";
            case DEFAULT:
                if (def instanceof TableDefinition) {
                    return "T" + name;
                }
                return name;
            default:
                return name;
        }
    }

    protected String getOutputName(Definition def) {
        return def.getOutputName();
    }

}
