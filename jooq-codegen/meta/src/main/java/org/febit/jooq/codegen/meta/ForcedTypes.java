package org.febit.jooq.codegen.meta;

import lombok.experimental.UtilityClass;
import org.jooq.meta.jaxb.ForcedType;

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

    public static void jsonToBeanMap(Sink sink, String expr, String beanType) {
        var call = ".forBeanMap(" + beanType + ".class)";
        var javaType = "java.util.Map<String, " + beanType + ">";
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
        datetimeToInstant(sink);
        timestampToInstant(sink);
        zonedDateTimeToInstant(sink);
    }

    public static void datetimeToInstant(Sink sink) {
        toInstant(sink, Types.DATETIME, null);
    }

    public static void timestampToInstant(Sink sink) {
        toInstant(sink, Types.TIMESTAMP, null);
    }

    public static void zonedDateTimeToInstant(Sink sink) {
        sink.accept(next()
                .converter(Converters.OFFSET_DATE_TIME_INSTANT)
                .expr(EXPR_ALL)
                .javaType(Instant.class.getName())
                .type(Types.TIMESTAMP_TZ)
        );
    }

    public static void toInstant(Sink sink, String type, String expr) {
        sink.accept(next()
                .converter(Converters.LOCAL_DATE_TIME_INSTANT)
                .expr(expr != null ? expr : EXPR_ALL)
                .javaType(Instant.class.getName())
                .type(type)
        );
    }

    public interface Converters {
        String VALUED_ENUM = "org.febit.boot.jooq.converter.";
        String LOCAL_DATE_TIME_INSTANT = "org.febit.boot.jooq.converter.LocalDateTimeToInstantConverter";
        String OFFSET_DATE_TIME_INSTANT = "org.febit.boot.jooq.converter.OffsetDateTimeToInstantConverter";
        String JSON_STRING = "org.febit.boot.jooq.converter.JsonStringConverter";
        String JSON = "org.febit.boot.jooq.converter.JsonConverter";
        String JSONB = "org.febit.boot.jooq.converter.JsonbConverter";
    }

    public interface Types {
        String ANY = ".*";
        String BOOLEAN = "boolean";
        String DATETIME = "datetime";
        String TIMESTAMP = "timestamp";

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
