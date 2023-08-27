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
import org.jooq.JSONB;
import org.jooq.impl.AbstractConverter;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class JsonbConverter<V> extends AbstractConverter<JSONB, V> {

    private final JavaType beanJsonType;

    private JsonbConverter(Class<V> toType, JavaType beanJsonType) {
        super(JSONB.class, toType);
        this.beanJsonType = beanJsonType;
    }

    @SuppressWarnings({"rawtypes"})
    public static <V> JsonbConverter<Map<String, V>> forBeanMap(Class<V> beanType) {
        return forBeanMap(String.class, beanType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> JsonbConverter<Map<K, V>> forBeanMap(Class<K> keyType, Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructMapType(
                Map.class, keyType, beanType);
        return new JsonbConverter(Map.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonbConverter<List<V>> forBeanList(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructCollectionType(
                List.class, beanType);
        return new JsonbConverter(List.class, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonbConverter<V[]> forBeanArray(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructArrayType(beanType);
        return new JsonbConverter(Array.newInstance(beanType, 0).getClass(), type);
    }

    public static <V> JsonbConverter<V> forBean(Class<V> beanType) {
        var type = JacksonUtils.TYPE_FACTORY.constructType(beanType);
        return new JsonbConverter<>(beanType, type);
    }

    @Nullable
    @Override
    public V from(@Nullable JSONB dbObj) {
        if (dbObj == null) {
            return null;
        }
        return JacksonUtils.parse(dbObj.data(), beanJsonType);
    }

    @Nullable
    @Override
    public JSONB to(@Nullable V customObj) {
        if (customObj == null) {
            return null;
        }
        return JSONB.valueOf(JacksonUtils.toJsonString(customObj));
    }
}
