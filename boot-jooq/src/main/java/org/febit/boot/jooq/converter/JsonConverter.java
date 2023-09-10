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
package org.febit.boot.jooq.converter;

import com.fasterxml.jackson.databind.JavaType;
import jakarta.annotation.Nullable;
import org.febit.lang.util.JacksonUtils;
import org.jooq.JSON;
import org.jooq.impl.AbstractConverter;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class JsonConverter<V> extends AbstractConverter<JSON, V> {

    private final JavaType beanJsonType;

    private JsonConverter(Class<V> toType, JavaType beanJsonType) {
        super(JSON.class, toType);
        this.beanJsonType = beanJsonType;
    }

    public static <V> JsonConverter<Map<String, V>> forBeanMap(Class<V> beanType) {
        return forBeanMap(String.class, beanType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> JsonConverter<Map<K, V>> forBeanMap(Class<K> keyType, Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructMapType(
                Map.class, keyType, beanType);
        return new JsonConverter(Map.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonConverter<List<V>> forBeanList(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructCollectionType(
                List.class, beanType);
        return new JsonConverter(List.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonConverter<V[]> forBeanArray(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructArrayType(beanType);
        return new JsonConverter(Array.newInstance(beanType, 0).getClass(), type);
    }

    public static <V> JsonConverter<V> forBean(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructType(beanType);
        return new JsonConverter<>(beanType, type);
    }

    @Nullable
    @Override
    public V from(@Nullable JSON dbObj) {
        if (dbObj == null) {
            return null;
        }
        return JacksonUtils.parse(dbObj.data(), beanJsonType);
    }

    @Nullable
    @Override
    public JSON to(@Nullable V customObj) {
        if (customObj == null) {
            return null;
        }
        return JSON.valueOf(JacksonUtils.toJsonString(customObj));
    }
}
