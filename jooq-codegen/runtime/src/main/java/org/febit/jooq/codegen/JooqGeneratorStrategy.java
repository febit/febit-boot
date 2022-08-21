package org.febit.jooq.codegen;

import lombok.extern.slf4j.Slf4j;
import org.febit.jooq.codegen.spi.ImplementsResolver;
import org.febit.jooq.codegen.spi.Naming;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.*;

import java.util.List;

@SuppressWarnings({"unused"})
@Slf4j
public class JooqGeneratorStrategy extends DefaultGeneratorStrategy {

    private static final List<Naming> NAMINGS = SpiUtils.load(Naming.class);
    private static final List<ImplementsResolver> IMPLS_RESOLVERS = SpiUtils.load(ImplementsResolver.class);

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
        var context = new ImplementsResolver.Context(this, def, mode, impls::add);
        IMPLS_RESOLVERS.forEach(r -> r.resolve(context));
        return impls;
    }

    @Override
    public String getJavaIdentifier(Definition def) {
        for (var naming : NAMINGS) {
            var name = naming.getIdentifier(def);
            if (name != null) {
                return name;
            }
        }
        return super.getJavaIdentifier(def);
    }

    @Override
    public String getJavaMemberName(Definition def, Mode mode) {
        for (var resolver : NAMINGS) {
            var name = resolver.getJavaMemberName(def, mode);
            if (name != null) {
                return name;
            }
        }
        return super.getJavaMemberName(def, mode);
    }

    @Override
    public String getJavaPackageName(Definition def, Mode mode) {
        var targetPkg = getTargetPackage();
        if (def.getDatabase().getCatalogs().size() > 1) {
            targetPkg += "." + getJavaIdentifier(def.getCatalog()).toLowerCase();
        }
        for (var resolver : NAMINGS) {
            var name = resolver.getPackageName(targetPkg, def, mode);
            if (name != null) {
                return name;
            }
        }
        return super.getJavaPackageName(def, mode);
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

        for (var resolver : NAMINGS) {
            var name = resolver.getClassName(def, mode);
            if (name != null) {
                return name;
            }
        }
        throw new IllegalStateException("Should not run to here!");
    }

}
