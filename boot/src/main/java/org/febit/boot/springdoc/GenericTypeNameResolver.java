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
package org.febit.boot.springdoc;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.core.util.PrimitiveType;

import java.util.Set;

/**
 * Override TypeNameResolver, wrap generic param types with {@code '<'} and {@code '>'}
 */
public class GenericTypeNameResolver extends TypeNameResolver {

    public static final GenericTypeNameResolver INSTANCE = new GenericTypeNameResolver();

    @Override
    protected String nameForGenericType(JavaType type, Set<Options> options) {
        var raw = nameForClass(type, options);
        var count = type.containedTypeCount();
        if (count == 0) {
            return raw;
        }
        var buf = new StringBuilder(raw);
        buf.append('<');
        for (int i = 0; i < count; ++i) {
            if (i != 0) {
                buf.append(',');
            }
            var param = type.containedType(i);
            buf.append(
                    PrimitiveType.fromType(param) != null
                            ? nameForClass(param, options)
                            : nameForType(param, options)
            );
        }
        buf.append('>');
        return buf.toString();
    }
}
