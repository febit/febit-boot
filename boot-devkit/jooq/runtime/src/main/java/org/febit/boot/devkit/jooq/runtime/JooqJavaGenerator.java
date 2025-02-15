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

import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.febit.boot.devkit.jooq.runtime.spi.TableFilter;
import org.febit.lang.util.JacksonUtils;
import org.jooq.codegen.FebitDevkitJavaGeneratorHack;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Database;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.TableDefinition;

import java.util.List;

@SuppressWarnings({"unused"})
public class JooqJavaGenerator extends FebitDevkitJavaGeneratorHack {

    private static final List<TableFilter> TABLE_FILTERS = SpiUtils.load(TableFilter.class);

    @Override
    protected void initDatabase(Database db) {
        TABLE_FILTERS.forEach(db::addFilter);
    }

    @Override
    protected void generateRecordClassFooter(TableDefinition table, JavaWriter out) {
        emitRecordFromPojoMethod(table, out);
        super.generateRecordClassFooter(table, out);
    }

    @Override
    protected void generateTableReferencesClassFooter(SchemaDefinition schema, JavaWriter out) {
        emitTableReferencesNamesClass(schema, out);
        super.generateTableReferencesClassFooter(schema, out);
    }

    @Override
    protected void generatePojoClassFooter(TableDefinition table, JavaWriter out) {
        emitPojoIdMethod(table, out);
        emitPojoToRecordMethod(table, out);
        super.generatePojoClassFooter(table, out);
    }

    @Override
    protected void generateTableClassFooter(TableDefinition table, JavaWriter out) {
        emitTableGetIdFieldMethod(table, out);
        emitTablePkExcludedFieldsMethod(table, out);
        emitTableColumnsClass(table, out);
        super.generateTableClassFooter(table, out);
    }

    /**
     * Emit Table#pkField() method.
     */
    private void emitTableGetIdFieldMethod(TableDefinition table, JavaWriter out) {

        var idColumn = JooqGeneratorStrategy.getPkColumn(table);
        if (idColumn == null) {
            return;
        }

        var idTypeFull = resolveColumnFullType(idColumn, out);
        var idType = out.ref(idTypeFull);
        var idColumnId = getStrategy().getJavaIdentifier(idColumn);

        out.println();
        out.println("public Field<%s> pkField() {", idType);
        out.println("return %s;", idColumnId);
        out.println("}");
    }

    /**
     * Emit Table#pkExcludedFields() method.
     */
    private void emitTablePkExcludedFieldsMethod(TableDefinition table, JavaWriter out) {
        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);

        var columns = table.getColumns().stream()
                .filter(col -> !col.isIdentity())
                .toList();

        out.println();
        out.ref(List.class);

        out.println("private final List<TableField<%s, ?>> pkExcludedFields = List.of(", recordClassName);
        var lastIdx = columns.size() - 1;
        for (int i = 0; i < columns.size(); i++) {
            var columnId = getStrategy().getJavaIdentifier(columns.get(i));
            out.println(i == lastIdx ? "%s" : "%s,", columnId);
        }
        out.println(");");

        out.println();
        out.println("public List<TableField<%s, ?>> pkExcludedFields() {", recordClassName);
        out.println("return pkExcludedFields;");
        out.println("}");
    }

    /**
     * Emit Table.Columns class.
     */
    private void emitTableColumnsClass(TableDefinition table, JavaWriter out) {
        out.println();
        out.println("public static class Columns {");
        for (var column : table.getColumns()) {
            var columnId = getStrategy().getJavaIdentifier(column);
            var columnName = column.getName();
            out.println("public static final String %s = %s;", columnId, JacksonUtils.toJsonString(columnName));
        }
        out.println("}");
    }

    /**
     * Emit Pojo#id() method.
     */
    private void emitPojoIdMethod(TableDefinition table, JavaWriter out) {

        var idColumn = JooqGeneratorStrategy.getPkColumn(table);
        if (idColumn == null) {
            return;
        }

        var idTypeFull = resolveColumnFullType(idColumn, out);
        var idType = out.ref(idTypeFull);
        var idGetter = getStrategy().getJavaGetterName(idColumn, GeneratorStrategy.Mode.POJO);

        out.println();
        out.println("@Override");
        out.println("public %s id() {", idType);
        out.println("return %s();", idGetter);
        out.println("}");
    }

    /**
     * Emit Pojo#toRecord() method.
     */
    private void emitPojoToRecordMethod(TableDefinition table, JavaWriter out) {

        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);
        var impledEntity = getStrategy().getJavaClassImplements(table, GeneratorStrategy.Mode.POJO)
                .stream()
                .anyMatch(cls -> cls.startsWith(MetaUtils.CORE_PKG + ".IEntity<"));

        out.ref(getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.RECORD));
        out.println();
        if (impledEntity) {
            out.println("@Override");
        }
        out.println("public %s toRecord() {", recordClassName);
        out.println("return %s.fromPojo(this);", recordClassName);
        out.println("}");
    }

    private String resolveColumnFullType(ColumnDefinition column, JavaWriter out) {
        var typeDef = column.getType(resolver(out, GeneratorStrategy.Mode.POJO));
        return getJavaType(typeDef, out, GeneratorStrategy.Mode.POJO);
    }

    /**
     * Emit Tables.Names class.
     */
    private void emitTableReferencesNamesClass(SchemaDefinition schema, JavaWriter out) {
        out.println();
        out.println("public static class Names {");
        for (var table : schema.getTables()) {
            var id = getStrategy().getJavaIdentifier(table);
            var name = table.getName();
            out.println("public static final String %s = %s;", id, JacksonUtils.toJsonString(name));
        }
        out.println("}");
    }

    /**
     * Emit Record#fromPojo(PO) method.
     */
    private void emitRecordFromPojoMethod(TableDefinition table, JavaWriter out) {
        var poClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.POJO);
        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);

        out.ref(getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.POJO));

        out.println();
        out.println("public static %s fromPojo(%s po) {", recordClassName, poClassName);
        out.println("%s record = new %s();", recordClassName, recordClassName);
        out.println("record.from(po);");
        out.println("return record;");
        out.println("}");
    }
}
