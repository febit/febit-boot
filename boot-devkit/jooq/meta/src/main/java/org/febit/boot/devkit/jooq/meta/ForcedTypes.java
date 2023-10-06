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
package org.febit.boot.devkit.jooq.meta;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.febit.lang.modeler.Schema;
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

    public static void to(Sink sink, String expr, Schema schema) {
        switch (schema.type()) {
            case BOOLEAN:
                toBoolean(sink, expr);
                break;
            case INSTANT:
                timeToInstant(sink, expr);
                break;
            case ENUM:
                toEnum(sink, expr, schema.valueType().toJavaTypeString());
                break;
            case JSON:
                jsonTo(sink, expr, schema.valueType());
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
            case DATETIME:
            case DATETIME_ZONED:
            case ARRAY:
            case LIST:
            case MAP:
            default:
                throw new IllegalArgumentException("Not support json type: " + schema.toJavaTypeString());
        }
    }

    public static void jsonTo(Sink sink, String expr, Schema type) {
        switch (type.type()) {
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
            case DATETIME:
            case DATETIME_ZONED:
                jsonToBean(sink, expr, type.toJavaTypeString());
                break;
            case ARRAY:
                jsonToBeanArray(sink, expr, type.valueType().toJavaTypeString());
                break;
            case LIST:
                jsonToBeanList(sink, expr, type.valueType().toJavaTypeString());
                break;
            case MAP:
                jsonToBeanMap(sink, expr,
                        type.keyType().toJavaTypeString(),
                        type.valueType().toJavaTypeString()
                );
                break;
            case JSON:
            default:
                throw new IllegalArgumentException("Not support json type: " + type.toJavaTypeString());
        }
    }

    public static void toBoolean(Sink sink, String expr) {
        sink.accept(next()
                .expr(expr)
                .name(TypePatterns.BOOLEAN)
                .type(TypePatterns.ANY)
        );
    }

    public static void toEnum(Sink sink, String expr, String enumType) {
        sink.accept(next()
                .converter(Converters.VALUED_ENUM + ".forEnum(" + enumType + ".class)")
                .expr(expr)
                .javaType(enumType)
                .type(TypePatterns.ANY)
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
                .type(TypePatterns.JSONB)
        );

        sink.accept(next()
                .converter(Converters.JSON + call)
                .expr(expr)
                .javaType(beanType)
                .type(TypePatterns.JSON)
        );

        sink.accept(next()
                .converter(Converters.JSON_STRING + call)
                .expr(expr)
                .javaType(beanType)
                .type(TypePatterns.STRING)
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
                .type(TypePatterns.DATETIME_OR_TIMESTAMP)
        );
        sink.accept(next()
                .converter(Converters.OFFSET_DATE_TIME_INSTANT)
                .expr(EXPR_ALL)
                .javaType(Instant.class.getName())
                .type(TypePatterns.TIMESTAMP_TZ)
        );
    }

    public interface Converters {
        String PKG = MetaUtils.CORE_PKG + ".converter.";
        String VALUED_ENUM = PKG + "ValuedEnumConverter";
        String LOCAL_DATE_TIME_INSTANT = PKG + "LocalDateTimeToInstantConverter";
        String OFFSET_DATE_TIME_INSTANT = PKG + "OffsetDateTimeToInstantConverter";
        String JSON_STRING = PKG + "JsonStringConverter";
        String JSON = PKG + "JsonConverter";
        String JSONB = PKG + "JsonbConverter";
    }

    public interface TypePatterns {
        String ANY = ".*";
        String BOOLEAN = "boolean";

        String DATETIME = "datetime";
        String TIMESTAMP = "timestamp";
        String DATETIME_OR_TIMESTAMP = DATETIME + "|" + TIMESTAMP;

        String TIMESTAMP_TZ = "("
                + "timestamptz|timestamp_tz"
                + "|(timestamp(\\s|_)?with(\\s|_)?time(\\s|_)?zone)"
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
