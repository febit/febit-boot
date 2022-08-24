package org.febit.jooq.codegen;

import org.febit.jooq.codegen.meta.MetaUtils;
import org.febit.jooq.codegen.spi.DatabaseFilter;
import org.jooq.codegen.FebitDevkitJavaGeneratorHack;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Database;
import org.jooq.meta.TableDefinition;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public class JooqJavaGenerator extends FebitDevkitJavaGeneratorHack {

    private static final List<DatabaseFilter> DB_FILTERS = SpiUtils.load(DatabaseFilter.class);

    @Override
    protected void initDatabase(Database db) {
        DB_FILTERS.forEach(db::addFilter);
    }

    @Override
    protected void generateRecordClassFooter(TableDefinition table, JavaWriter out) {
        emitRecordFromPojoMethod(table, out);
        super.generateRecordClassFooter(table, out);
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
        out.tab(1).println("public Field<%s> pkField() {", idType);
        out.tab(2).println("return %s;", idColumnId);
        out.tab(1).println("}");
    }

    /**
     * Emit Table#pkExcludedFields() method.
     */
    private void emitTablePkExcludedFieldsMethod(TableDefinition table, JavaWriter out) {
        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);
        out.println();
        out.ref(Collectors.class);
        out.ref(List.class);
        out.tab(1).println("private List<TableField<%s, ?>> __FIELDS_EXCLUDED_PKS;", recordClassName);
        out.println();
        out.tab(1).println("public List<TableField<%s, ?>> pkExcludedFields() {", recordClassName);
        out.tab(2).println("List<TableField<%s, ?>> fields = __FIELDS_EXCLUDED_PKS;", recordClassName);
        out.tab(2).println("return fields != null ? fields");
        out.tab(4).println(": (__FIELDS_EXCLUDED_PKS = fieldStream()");
        out.tab(4).println(".map(f -> (TableField<%s, ?>) f)", recordClassName);
        out.tab(4).println(".filter(f -> !f.getDataType().identity())");
        out.tab(4).println(".collect(Collectors.toList()));");
        out.tab(1).println("}");
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
        out.tab(1).println("@Override");
        out.tab(1).println("public %s id() {", idType);
        out.tab(2).println("return %s();", idGetter);
        out.tab(1).println("}");
    }

    /**
     * Emit Pojo#toRecord() method.
     */
    private void emitPojoToRecordMethod(TableDefinition table, JavaWriter out) {

        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);
        var impledEntity = getStrategy().getJavaClassImplements(table, GeneratorStrategy.Mode.POJO)
                .stream()
                .anyMatch(cls -> cls.startsWith(MetaUtils.JOOQ_PKG + ".IEntity<"));

        out.ref(getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.RECORD));
        out.println();
        if (impledEntity) {
            out.tab(1).println("@Override");
        }
        out.tab(1).println("public %s toRecord() {", recordClassName);
        out.tab(2).println("return %s.fromPojo(this);", recordClassName);
        out.tab(1).println("}");
    }


    /**
     * Emit Record#fromPojo(..) method.
     */
    private void emitRecordFromPojoMethod(TableDefinition table, JavaWriter out) {
        var poClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.POJO);
        var recordClassName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.RECORD);

        out.ref(getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.POJO));

        out.println();
        out.tab(1).println("public static %s fromPojo(%s po) {", recordClassName, poClassName);
        out.tab(2).println("%s record = new %s();", recordClassName, recordClassName);
        out.tab(2).println("record.from(po);");
        out.tab(2).println("return record;");
        out.tab(1).println("}");
    }

    private String resolveColumnFullType(ColumnDefinition column, JavaWriter out) {
        var typeDef = column.getType(resolver(out, GeneratorStrategy.Mode.POJO));
        return getJavaType(typeDef, out, GeneratorStrategy.Mode.POJO);
    }
}
