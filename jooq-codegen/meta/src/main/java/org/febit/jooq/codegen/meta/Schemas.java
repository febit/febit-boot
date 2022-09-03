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
package org.febit.jooq.codegen.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.febit.lang.util.StringWalker;

/**
 * Internal schema utils.
 */
@UtilityClass
class Schemas {

    private static final String TYPE_NAME_END_CHARS = "\r\n \t\f\b<>[],:;+-=#";

    private static boolean isTypeNameEnding(char c) {
        return TYPE_NAME_END_CHARS.indexOf(c) >= 0;
    }

    static Schema parse(String str) {
        var walker = new StringWalker(str);
        return readType(walker);
    }

    private static Schema readType(StringWalker walker) {
        walker.skipBlanks();
        var typeName = walker.readToFlag(Schemas::isTypeNameEnding, true);
        switch (typeName.toLowerCase()) {
            case "int":
            case "integer":
                return PrimitiveSchema.of(Schema.Type.INT);
            case "long":
                return PrimitiveSchema.of(Schema.Type.LONG);
            case "string":
                return PrimitiveSchema.of(Schema.Type.STRING);
            case "boolean":
                return PrimitiveSchema.of(Schema.Type.BOOLEAN);
            case "bytes":
                return PrimitiveSchema.of(Schema.Type.BYTES);
            case "float":
                return PrimitiveSchema.of(Schema.Type.FLOAT);
            case "double":
                return PrimitiveSchema.of(Schema.Type.DOUBLE);
            case "date":
            case "localdate":
                return PrimitiveSchema.of(Schema.Type.DATE);
            case "time":
            case "localtime":
                return PrimitiveSchema.of(Schema.Type.TIME);
            case "instant":
                return PrimitiveSchema.of(Schema.Type.INSTANT);
            case "datetime":
            case "localdatetime":
                return PrimitiveSchema.of(Schema.Type.LOCAL_DATETIME);
            case "timestampz":
            case "datetimez":
            case "zoneddatetime":
                return PrimitiveSchema.of(Schema.Type.ZONED_DATETIME);
            case "array":
                return readElementSchema(Schema.Type.ARRAY, walker);
            case "list":
                return readElementSchema(Schema.Type.LIST, walker);
            case "enum":
                return readElementSchema(Schema.Type.ENUM, walker);
            case "map":
                return readMapSchema(walker);
            default:
                return RawSchema.of(typeName);
        }
    }

    private static Schema readElementSchema(Schema.Type type, StringWalker walker) {
        walker.skipBlanks();

        var colon = walker.peek() == ':';
        if (colon) {
            walker.jump(1);
        } else {
            walker.requireAndJumpChar('<');
        }

        walker.skipBlanks();
        var elementType = readType(walker);
        walker.skipBlanks();

        if (!colon) {
            walker.requireAndJumpChar('>');
        }

        return ElementSchema.of(type, elementType);
    }

    private static Schema readMapSchema(StringWalker walker) {
        walker.skipBlanks();
        walker.requireAndJumpChar('<');
        walker.skipBlanks();
        var keyType = readType(walker);
        walker.skipBlanks();
        walker.requireAndJumpChar(',');
        walker.skipBlanks();
        var valueType = readType(walker);
        walker.requireAndJumpChar('>');
        return MapSchema.of(keyType, valueType);
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class PrimitiveSchema implements Schema {
        private final Schema.Type type;

        @Override
        public String toTypeString() {
            return type.toTypeString();
        }
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class RawSchema implements Schema {
        private final Schema.Type type = Type.RAW;
        private final String raw;

        @Override
        public String toTypeString() {
            return raw;
        }
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class ElementSchema implements Schema {
        private final Schema.Type type;
        private final Schema valueType;

        @Override
        public String toTypeString() {
            var valueTypeStr = valueType.toTypeString();
            switch (type) {
                case ARRAY:
                    return valueTypeStr + "[]";
                case ENUM:
                    return valueTypeStr;
                default:
                    return type.toTypeString() + '<'
                            + valueType.toTypeString()
                            + '>';
            }
        }

    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class MapSchema implements Schema {
        private final Schema.Type type = Type.MAP;
        private final Schema keyType;
        private final Schema valueType;

        @Override
        public String toTypeString() {
            return type.toTypeString() + '<'
                    + keyType.toTypeString()
                    + ", "
                    + valueType.toTypeString()
                    + '>';
        }
    }
}
