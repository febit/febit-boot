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

import java.time.*;
import java.util.List;
import java.util.Map;

/**
 * Schema.
 */
public interface Schema {

    static Schema parse(String str) {
        return Schemas.parse(str);
    }

    Type getType();

    String toTypeString();

    default Schema getKeyType() {
        throw new UnsupportedOperationException("Not map optional: " + this);
    }

    default Schema getValueType() {
        throw new UnsupportedOperationException("Not an array / list / map: " + this);
    }

    @RequiredArgsConstructor
    enum Type {

        ARRAY(Object[].class),
        LIST(List.class),
        MAP(Map.class),
        ENUM(Enum.class),
        JSON(Object.class),

        RAW(Object.class),

        STRING(String.class),
        BYTES(byte[].class),
        BOOLEAN(Boolean.class),
        INT(Integer.class),
        LONG(Long.class),
        FLOAT(Float.class),
        DOUBLE(Double.class),

        INSTANT(Instant.class),
        DATE(LocalDate.class),
        TIME(LocalTime.class),
        LOCAL_DATETIME(LocalDateTime.class),
        ZONED_DATETIME(ZonedDateTime.class),
        ;

        @Getter
        private final Class<?> javaType;

        public String toTypeString() {
            if ("java.lang".equals(javaType.getPackageName())) {
                return javaType.getSimpleName();
            }
            return javaType.getCanonicalName();
        }
    }

}
