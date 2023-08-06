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
package org.febit.boot.devkit.jooq.runtime;

import lombok.extern.slf4j.Slf4j;
import org.febit.boot.devkit.jooq.runtime.spi.ClassNameDecorator;
import org.febit.boot.devkit.jooq.runtime.spi.ImplementsResolver;
import org.febit.boot.devkit.jooq.runtime.spi.Naming;
import org.febit.boot.devkit.jooq.runtime.spi.OutputNameResolver;
import org.febit.boot.devkit.jooq.runtime.util.NamingUtils;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.CatalogDefinition;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.TableDefinition;

import java.util.List;

@SuppressWarnings({"unused"})
@Slf4j
public class JooqGeneratorStrategy extends DefaultGeneratorStrategy {

    private final List<ImplementsResolver> implsResolvers = SpiUtils.load(ImplementsResolver.class, this);

    private final Spi<Naming> namingSpi = Spi.load(Naming.class, this);
    private final Spi<OutputNameResolver> outputNameResolverSpi = Spi.load(OutputNameResolver.class, this);
    private final Spi<ClassNameDecorator> classNameDecoratorSpi = Spi.load(ClassNameDecorator.class, this);

    public static ColumnDefinition getPkColumn(TableDefinition table) {
        var pk = table.getPrimaryKey();
        if (pk == null) {
            return null;
        }
        var pks = pk.getKeyColumns();
        if (pks.size() != 1) {
            return null;
        }
        return pks.get(0);
    }

    public String resolveOutputName(Definition definition) {
        return outputNameResolverSpi.compute(
                r -> r.resolve(definition),
                definition::getOutputName
        );
    }

    public String resolveColumnType(ColumnDefinition column) {
        DataTypeDefinition type = column.getType();
        String fixed = type.getType().toLowerCase();
        int split = fixed.indexOf('(');
        if (split >= 0) {
            fixed = fixed.substring(0, split)
                    .trim();
        }
        switch (fixed) {
            case "long":
            case "bigint":
                return "Long";
            case "int":
            case "integer":
                return "Integer";
            case "string":
            case "text":
            case "char":
            case "varchar":
            case "character":
            case "character varying":
            case "varchar_ignorecase":
                return "String";
            default:
        }
        return type.getType();
    }

    @Override
    public List<String> getJavaClassImplements(Definition def, Mode mode) {
        var impls = super.getJavaClassImplements(def, mode);
        var context = new ImplementsResolver.ContextImpl(this, def, mode, impls::add);
        implsResolvers.forEach(r -> r.resolve(context));
        return impls;
    }

    @Override
    public String getJavaIdentifier(Definition def) {
        var name = namingSpi.compute(n -> n.identifier(def));
        if (name != null) {
            return name;
        }
        return super.getJavaIdentifier(def);
    }

    @Override
    public String getJavaMemberName(Definition def, Mode mode) {
        var name = namingSpi.compute(n -> n.memberField(def, mode));
        if (name != null) {
            return name;
        }
        return super.getJavaMemberName(def, mode);
    }

    @Override
    public String getJavaPackageName(Definition def, Mode mode) {

        var targetPkg = def.getDatabase().getCatalogs().size() <= 1
                ? getTargetPackage()
                : getTargetPackage() + "." + getJavaIdentifier(def.getCatalog()).toLowerCase();

        return namingSpi.compute(
                n -> n.getPackageName(targetPkg, def, mode),
                () -> super.getJavaPackageName(def, mode)
        );
    }

    @Override
    public String getJavaClassName(Definition def, Mode mode) {

        if (def instanceof CatalogDefinition
                && ((CatalogDefinition) def).isDefaultCatalog()) {
            return "DefaultCatalog";
        }

        if (def instanceof SchemaDefinition
                && ((SchemaDefinition) def).isDefaultSchema()) {
            return "DefaultSchema";
        }

        var name = namingSpi.compute(
                n -> n.className(def),
                () -> NamingUtils.toUpperCamelCase(
                        resolveOutputName(def)
                )
        );

        return classNameDecoratorSpi.compute(
                d -> d.decorate(def, name, mode),
                () -> name
        );
    }

}
