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

import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;
import java.util.ServiceLoader;

@UtilityClass
public class I18nUtils {

    private static final String[] BASE_NAMES = {
            "i18n/messages",
    };

    private static final MessageSource MESSAGE_SOURCE;

    static {
        var source = new ResourceBundleMessageSource();
        source.setBasenames(BASE_NAMES);
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);

        ServiceLoader.load(I18nMessageLocation.class)
                .forEach(p ->
                        p.locations().forEach(source::addBasenames)
                );
        MESSAGE_SOURCE = source;
    }

    /**
     * Format the given arguments with the given pattern/code.
     *
     * @param code pattern/code
     * @param args arguments
     * @see LocaleContextHolder#getLocale()
     * @see MessageSource#getMessage(String, Object[], String, Locale)
     */
    public static String format(final String code, final Object... args) {
        var locale = LocaleContextHolder.getLocale();
        var msg = MESSAGE_SOURCE.getMessage(code, args, code, locale);
        return msg != null ? msg : code;
    }
}
