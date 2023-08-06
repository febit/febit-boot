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
package org.febit.boot.devkit.jooq.runtime.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jooq.tools.StringUtils;

@UtilityClass
public class NamingUtils {

    public static String toLowerCamelCase(String name) {
        val camel = toUpperCamelCase(name);
        return lowerFirst(camel);
    }

    public static String toUpperCamelCase(String name) {
        name = org.apache.commons.lang3.StringUtils
                .replaceChars(name, " -.", "___");

        return StringUtils.toCamelCase(name);
    }

    public static String lowerFirst(String name) {
        return StringUtils.toLC(name);
    }

    public static String upperFirst(String name) {
        return StringUtils.toUC(name);
    }
}
