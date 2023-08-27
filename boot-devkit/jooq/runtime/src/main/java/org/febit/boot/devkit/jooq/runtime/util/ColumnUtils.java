package org.febit.boot.devkit.jooq.runtime.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.TableDefinition;

import java.util.function.Predicate;

@UtilityClass
public class ColumnUtils {

    public static final String TYPE_INTEGER = "Integer";
    public static final String TYPE_LONG = "Long";
    public static final String TYPE_INSTANT = "Instant";
    public static final String TYPE_STRING = "String";

    public static boolean check(TableDefinition table, String name, Predicate<ColumnDefinition> predicate) {
        var column = table.getColumn(name, true);
        if (column == null) {
            return false;
        }
        return predicate.test(column);
    }

    public static boolean isInteger(TableDefinition table, String name) {
        return check(table, name, ColumnUtils::isInteger);
    }

    public static boolean isLong(TableDefinition table, String name) {
        return check(table, name, ColumnUtils::isLong);
    }

    public static boolean isString(TableDefinition table, String name) {
        return check(table, name, ColumnUtils::isString);
    }

    public static boolean isInstant(TableDefinition table, String name) {
        return check(table, name, ColumnUtils::isInstant);
    }

    public static boolean isInteger(ColumnDefinition column) {
        return TYPE_INTEGER.equals(type(column));
    }

    public static boolean isLong(ColumnDefinition column) {
        return TYPE_LONG.equals(type(column));
    }

    public static boolean isString(ColumnDefinition column) {
        return TYPE_STRING.equals(type(column));
    }

    public static boolean isInstant(ColumnDefinition column) {
        return TYPE_INSTANT.equals(type(column));
    }

    public static String type(ColumnDefinition column) {
        var type = column.getType();
        var fixed = StringUtils.substringBefore(type.getType(), '(')
                .toLowerCase();
        return switch (fixed) {
            case "int", "integer" -> TYPE_INTEGER;
            case "long", "bigint" -> TYPE_LONG;
            case "datetime",
                    "timestamp",
                    "timestamptz",
                    "timestamp with time zone" -> TYPE_INSTANT;
            case "string",
                    "text",
                    "char",
                    "varchar",
                    "character",
                    "character varying",
                    "varchar_ignorecase" -> TYPE_STRING;
            default -> type.getType();
        };
    }
}
