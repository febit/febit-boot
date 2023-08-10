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
package org.febit.boot.devkit.feign.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.util.JacksonUtils;
import org.springframework.core.ResolvableType;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;

@UtilityClass
public class CodeUtils {

    public static String pkg(String fullName) {
        return StringUtils.substringBeforeLast(fullName, ".");
    }

    public static String classSimpleName(String fullName) {
        return StringUtils.substringAfterLast(fullName, ".");
    }

    public static boolean isDeprecated(@Nullable AnnotatedElement element) {
        return element != null && element.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isDeprecated(@Nullable PropertyDescriptor prop) {
        if (prop == null) {
            return false;
        }
        return isDeprecated(prop.getReadMethod())
                || isDeprecated(prop.getWriteMethod());
    }

    public static boolean isInPackage(String cls, String pkg) {
        int pkgLen = pkg.length();
        if (cls.length() <= pkgLen) {
            return false;
        }
        if (cls.charAt(pkgLen) != '.') {
            return false;
        }
        if (!cls.startsWith(pkg)) {
            return false;
        }
        return cls.indexOf('.', pkgLen + 1) < 0;
    }

    public static Class<?> resolveFinalComponentType(Class<?> cls) {
        if (!cls.isArray()) {
            return cls;
        }
        return resolveFinalComponentType(cls.getComponentType());
    }

    public static String toCodeString(String str) {
        return JacksonUtils.toJsonString(str);
    }

    public static String upperFirst(String ident) {
        return ident.substring(0, 1).toUpperCase()
                + ident.substring(1);
    }

    public static ResolvableType getPropertyResolvableType(PropertyDescriptor prop) {
        var getter = prop.getReadMethod();
        var setter = prop.getWriteMethod();
        return getter != null
                ? ResolvableType.forMethodReturnType(getter)
                : ResolvableType.forMethodParameter(setter, 0);
    }
}
