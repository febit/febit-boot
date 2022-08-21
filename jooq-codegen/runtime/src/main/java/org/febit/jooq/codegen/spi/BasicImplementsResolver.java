package org.febit.jooq.codegen.spi;

import org.febit.jooq.codegen.JooqGeneratorStrategy;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.TableDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class BasicImplementsResolver implements ImplementsResolver {

    @Override
    public void resolve(Context context) {
        if (!context.isTableDefinition()) {
            return;
        }

        var table = (TableDefinition) context.getDef();
        var pkCol = JooqGeneratorStrategy.getPkColumn(table);

        if (pkCol == null) {
            return;
        }

        var pkType = context.getStrategy().resolveColumnType(pkCol);
        var recordClassName = context.getStrategy()
                .getJavaClassName(table, GeneratorStrategy.Mode.RECORD);

        switch (context.getMode()) {
            case DEFAULT:
                context.addImpl("org.febit.jooq.ITable<" + recordClassName + ", " + pkType + ">");
                break;
            case POJO:
                context.addImpl("org.febit.jooq.IEntity<" + pkType + ">");
                break;
            default:
        }
    }

}
