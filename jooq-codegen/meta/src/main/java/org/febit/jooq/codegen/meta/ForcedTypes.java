package org.febit.jooq.codegen.meta;

import lombok.experimental.UtilityClass;
import org.jooq.meta.jaxb.ForcedType;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.function.Consumer;

@UtilityClass
public class ForcedTypes {

    public static final String EXPR_ALL = ".*";

    @lombok.Builder(
            builderMethodName = "next"
    )
    private static ForcedType forcedType(
            String name,
            String expr,
            String type,
            String javaType,
            String converter
    ) {
        return new ForcedType()
                .withName(name)
                .withConverter(converter)
                .withIncludeExpression(expr != null ? expr : EXPR_ALL)
                .withUserType(javaType)
                .withIncludeTypes(type);
    }

    public static void to(Sink sink, String expr, Schema schema) {
        switch (schema.getType()) {
            case BOOLEAN:
                toBoolean(sink, expr);
                break;
            case INSTANT:
                timeToInstant(sink, expr);
                break;
            case ENUM:
                toEnum(sink, expr, schema.getValueType().toTypeString());
                break;
            case JSON:
                jsonTo(sink, expr, schema.getValueType());
                break;
            case RAW:
            case STRING:
            case BYTES:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case DATE:
            case TIME:
            case LOCAL_DATETIME:
            case ZONED_DATETIME:
            case ARRAY:
            case LIST:
            case MAP:
            default:
                throw new IllegalArgumentException("Not support json type: " + schema.toTypeString());
        }
    }

    public static void jsonTo(Sink sink, String expr, Schema type) {
        switch (type.getType()) {
            case RAW:
            case ENUM:
            case STRING:
            case BYTES:
            case BOOLEAN:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case INSTANT:
            case DATE:
            case TIME:
            case LOCAL_DATETIME:
            case ZONED_DATETIME:
                jsonToBean(sink, expr, type.toTypeString());
                break;
            case ARRAY:
                jsonToBeanArray(sink, expr, type.getValueType().toTypeString());
                break;
            case LIST:
                jsonToBeanList(sink, expr, type.getValueType().toTypeString());
                break;
            case MAP:
                jsonToBeanMap(sink, expr,
                        type.getKeyType().toTypeString(),
                        type.getValueType().toTypeString()
                );
                break;
            case JSON:
            default:
                throw new IllegalArgumentException("Not support json type: " + type.toTypeString());
        }
    }

    public static void toBoolean(Sink sink, String expr) {
        sink.accept(next()
                .expr(expr)
                .name(Types.BOOLEAN)
                .type(Types.ANY)
        );
    }

    public static void toEnum(Sink sink, String expr, String enumType) {
        sink.accept(next()
                .converter(Converters.VALUED_ENUM + ".forEnum(" + enumType + ".class)")
                .expr(expr)
                .javaType(enumType)
                .type(Types.ANY)
        );
    }

    public static void jsonToBean(Sink sink, String expr, String beanType) {
        var call = ".forBean(" + beanType + ".class)";
        jsonToBean(sink, expr, beanType, call);
    }

    public static void jsonToBeanMap(Sink sink, String expr, String beanType) {
        var call = ".forBeanMap(" + beanType + ".class)";
        var javaType = "java.util.Map<String, " + beanType + ">";
        jsonToBean(sink, expr, javaType, call);
    }

    private void jsonToBean(Sink sink, String expr, String beanType, String call) {
        sink.accept(next()
                .converter(Converters.JSONB + call)
                .expr(expr)
                .javaType(beanType)
                .type(Types.JSONB)
        );

        sink.accept(next()
                .converter(Converters.JSON + call)
                .expr(expr)
                .javaType(beanType)
                .type(Types.JSON)
        );

        sink.accept(next()
                .converter(Converters.JSON_STRING + call)
                .expr(expr)
                .javaType(beanType)
                .type(Types.STRING)
        );
    }

    public static void jsonToBeanMap(Sink sink, String expr, String keyType, String beanType) {
        var call = ".forBeanMap(" + keyType + ".class, " + beanType + ".class)";
        var javaType = "java.util.Map<" + keyType + ", " + beanType + ">";
        jsonToBean(sink, expr, javaType, call);
    }

    public static void jsonToBeanArray(Sink sink, String expr, String beanType) {
        var call = ".forBeanArray(" + beanType + ".class)";
        var javaType = beanType + "[]";
        jsonToBean(sink, expr, javaType, call);
    }

    public static void jsonToBeanList(Sink sink, String expr, String beanType) {
        var call = ".forBeanList(" + beanType + ".class)";
        var javaType = "java.util.List<" + beanType + ">";
        jsonToBean(sink, expr, javaType, call);
    }

    public static void timeToInstant(Sink sink) {
        timeToInstant(sink, null);
    }

    public static void timeToInstant(Sink sink, @Nullable String expr) {
        sink.accept(next()
                .converter(Converters.LOCAL_DATE_TIME_INSTANT)
                .expr(expr != null ? expr : EXPR_ALL)
                .javaType(Instant.class.getName())
                .type(Types.DATETIME_OR_TIMESTAMP)
        );
        sink.accept(next()
                .converter(Converters.OFFSET_DATE_TIME_INSTANT)
                .expr(EXPR_ALL)
                .javaType(Instant.class.getName())
                .type(Types.TIMESTAMP_TZ)
        );
    }

    public interface Converters {
        String PKG = MetaUtils.JOOQ_PKG + ".converter.";
        String VALUED_ENUM = PKG + "ValuedEnumConverter";
        String LOCAL_DATE_TIME_INSTANT = PKG + "LocalDateTimeToInstantConverter";
        String OFFSET_DATE_TIME_INSTANT = PKG + "OffsetDateTimeToInstantConverter";
        String JSON_STRING = PKG + "JsonStringConverter";
        String JSON = PKG + "JsonConverter";
        String JSONB = PKG + "JsonbConverter";
    }

    public interface Types {
        String ANY = ".*";
        String BOOLEAN = "boolean";

        String DATETIME = "datetime";
        String TIMESTAMP = "timestamp";
        String DATETIME_OR_TIMESTAMP = DATETIME + "|" + TIMESTAMP;

        String TIMESTAMP_TZ = "("
                + "TIMESTAMPTZ|TIMESTAMP_TZ"
                + "|(TIMESTAMP(\\s|_)?WITH(\\s|_)?TIME(\\s|_)?ZONE)"
                + ")([(].*)?";

        String STRING = "("
                + "char|varchar|character"
                + "|varchar_ignorecase"
                + "|character varying"
                + "|text|string|clob"
                + ")([(].*)?";
        String JSONB = "jsonb";
        String JSON = "json";
    }

    @FunctionalInterface
    public interface Sink extends Consumer<ForcedType> {

        default void accept(ForcedTypeBuilder builder) {
            accept(builder.build());
        }
    }
}
