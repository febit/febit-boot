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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.tabletest.junit.TableTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nUtilsTest {

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @TableTest("""
        scenario                | code            | locale | args             | expected
        resolved plain          | greeting        | en     | []               | Hello
        resolved with arg       | greeting.name   | en     | [World]          | Hello World
        resolved multi args     | multi           | en     | [a, b]           | a and b
        locale override zh      | greeting.name   | zh_CN  | ['小明']          | 你好 小明
        zh-only resolves        | only.zh         | zh_CN  | []               | 中文
        zh-only unknown locale  | only.zh         | en     | []               | only.zh
        unresolved no args      | missing.code    | en     | []               | missing.code
        unresolved with args    | missing.code    | en     | [x]              | missing.code
        """)
    void format(String code, String locale, String[] args, String expected) {
        LocaleContextHolder.setLocale(parseLocale(locale));
        assertThat(I18nUtils.format(code, (Object[]) args)).isEqualTo(expected);
    }

    private static Locale parseLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return Locale.ENGLISH;
        }
        String[] parts = locale.split("_");
        return parts.length == 1 ? Locale.of(parts[0]) : Locale.of(parts[0], parts[1]);
    }

    @Test
    void formatReturnsCodeWhenUnresolved() {
        assertThat(I18nUtils.format("some.unknown.code")).isEqualTo("some.unknown.code");
    }
}
