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
package org.febit.jooq.converter;

import com.fasterxml.jackson.databind.JavaType;
import lombok.val;
import org.febit.util.JacksonUtils;
import org.jooq.impl.AbstractConverter;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class JsonStringConverter<V> extends AbstractConverter<String, V> {

    private final JavaType beanJsonType;

    private JsonStringConverter(Class<V> toType, JavaType beanJsonType) {
        super(String.class, toType);
        this.beanJsonType = beanJsonType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonStringConverter<Map<String, V>> forBeanMap(Class<V> beanType) {
        val type = JacksonUtils.TYPE_FACTORY.constructMapType(
                Map.class, String.class, beanType);
        return new JsonStringConverter(Map.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonStringConverter<List<V>> forBeanList(Class<V> beanType) {
        val type = JacksonUtils.TYPE_FACTORY.constructCollectionType(
                List.class, beanType);
        return new JsonStringConverter(List.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonStringConverter<V[]> forBeanArray(Class<V> beanType) {
        val type = JacksonUtils.TYPE_FACTORY.constructArrayType(beanType);
        return new JsonStringConverter(Array.newInstance(beanType, 0).getClass(), type);
    }

    public static <V> JsonStringConverter<V> forBean(Class<V> beanType) {
        val type = JacksonUtils.TYPE_FACTORY.constructType(beanType);
        return new JsonStringConverter<>(beanType, type);
    }

    @Nullable
    @Override
    public V from(@Nullable String dbObj) {
        if (dbObj == null || dbObj.isEmpty()) {
            return null;
        }
        return JacksonUtils.parse(dbObj, beanJsonType);
    }

    @Nullable
    @Override
    public String to(@Nullable V customObj) {
        if (customObj == null) {
            return null;
        }
        return JacksonUtils.toJsonString(customObj);
    }
}
