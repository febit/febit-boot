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
package org.febit.boot.okhttp3;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCookieJarTest {

    private static final HttpUrl URL = HttpUrl.get("https://example.com/");

    private static Cookie cookie(String header) {
        return Cookie.parse(URL, header);
    }

    @Test
    void loadReturnsEmptyBeforeSave() {
        var jar = TokenCookieJar.create("token");
        assertThat(jar.loadForRequest(URL)).isEmpty();
    }

    @Test
    void saveStoresMatchingCookie() {
        var jar = TokenCookieJar.create("token");
        var cookie = cookie("token=abc; Path=/");
        jar.saveFromResponse(URL, List.of(cookie));
        assertThat(jar.loadForRequest(URL)).containsExactly(cookie);
    }

    @Test
    void saveIgnoresNonMatchingCookies() {
        var jar = TokenCookieJar.create("token");
        jar.saveFromResponse(URL, List.of(cookie("session=xyz; Path=/")));
        assertThat(jar.loadForRequest(URL)).isEmpty();
    }

    @Test
    void saveOverwritesWithLatestMatchingCookie() {
        var jar = TokenCookieJar.create("token");
        var first = cookie("token=first; Path=/");
        var second = cookie("token=second; Path=/");
        jar.saveFromResponse(URL, List.of(first));
        jar.saveFromResponse(URL, List.of(second));
        assertThat(jar.loadForRequest(URL)).containsExactly(second);
    }

    @Test
    void savePicksLastMatchingWithinSameBatch() {
        var jar = TokenCookieJar.create("token");
        var a = cookie("token=a; Path=/");
        var b = cookie("token=b; Path=/");
        jar.saveFromResponse(URL, List.of(a, b));
        assertThat(jar.loadForRequest(URL)).containsExactly(b);
    }

    @Test
    void saveStoresOnlyMatchingAmongMixed() {
        var jar = TokenCookieJar.create("token");
        jar.saveFromResponse(URL, List.of(cookie("session=x; Path=/"), cookie("token=t; Path=/")));
        assertThat(jar.loadForRequest(URL)).containsExactly(cookie("token=t; Path=/"));
    }

    // 当前行为：不含 key 的批次不会清空已存 token（token 应答缺省时旧值保留）。
    @Test
    void saveNonMatchingBatchLeavesPreviousUntouched() {
        var jar = TokenCookieJar.create("token");
        jar.saveFromResponse(URL, List.of(cookie("token=keep; Path=/")));
        jar.saveFromResponse(URL, List.of(cookie("session=other; Path=/")));
        assertThat(jar.loadForRequest(URL)).containsExactly(cookie("token=keep; Path=/"));
    }
}
