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
package org.febit.boot.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

@UtilityClass
public class Models {

    private static final DefaultConversionService CONVERSION = new DefaultConversionService();

    /**
     * Copy the property values from source bean into the target bean.
     *
     * @param source   source bean
     * @param supplier supplier of target bean
     * @see Models#map(Object, Object)
     */
    public static <T> T map(@Nullable Object source, Supplier<T> supplier) {
        return map(source, supplier.get());
    }

    /**
     * Copy the property values from source bean into the target bean.
     *
     * @throws BeansException if the copying failed
     * @see BeanUtils#copyProperties(Object, Object)
     * @see DefaultConversionService#convert(Object, Class)
     */
    @SuppressWarnings({
            "squid:S135", // Loops should not contain more than a single "break" or "continue" statement
            "squid:MethodCyclomaticComplexity",
    })
    public static <T> T map(@Nullable Object source, T target) {
        if (source == null) {
            return target;
        }
        var sourceType = source.getClass();
        for (var targetProp : BeanUtils.getPropertyDescriptors(target.getClass())) {
            var writer = targetProp.getWriteMethod();
            if (writer == null) {
                continue;
            }
            var sourceProp = BeanUtils.getPropertyDescriptor(sourceType, targetProp.getName());
            if (sourceProp == null) {
                continue;
            }
            var reader = sourceProp.getReadMethod();
            if (reader == null) {
                continue;
            }
            try {
                setAccessible(reader);
                setAccessible(writer);
                writer.invoke(target, convertIfNeed(
                        reader.invoke(source),
                        writer.getParameterTypes()[0]
                ));
            } catch (Exception ex) {
                throw new FatalBeanException("Cannot copy value of property '"
                        + targetProp.getName()
                        + "' from source into target", ex);
            }
        }
        return target;
    }

    /**
     * Add a plain converter.
     *
     * @see ConverterRegistry#addConverter(Converter)
     */
    public static void addConverter(Converter<?, ?> converter) {
        CONVERSION.addConverter(converter);
    }

    /**
     * Add a plain converter.
     *
     * @see ConverterRegistry#addConverter(Class, Class, Converter)
     */
    public static <S, T> void addConverter(
            Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter
    ) {
        CONVERSION.addConverter(sourceType, targetType, converter);
    }

    /**
     * Add a generic converter.
     *
     * @see ConverterRegistry#addConverter(GenericConverter)
     */
    public static void addConverter(GenericConverter converter) {
        CONVERSION.addConverter(converter);
    }

    @Nullable
    private static Object convertIfNeed(@Nullable Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (ClassUtils.isAssignable(targetType, value.getClass())) {
            return value;
        }
        return CONVERSION.convert(value, targetType);
    }

    private static void setAccessible(Method method) {
        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }
}
