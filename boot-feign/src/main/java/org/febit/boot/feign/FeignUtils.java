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
package org.febit.boot.feign;

import feign.Response;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class FeignUtils {

    @Nullable
    public static byte[] read(Response resp) throws IOException {
        if (resp.body() == null) {
            return null;
        }
        return IOUtils.toByteArray(resp.body().asInputStream());
    }

    @Nullable
    public static String readUtf8(Response resp) throws IOException {
        if (resp.body() == null) {
            return null;
        }
        return IOUtils.toString(resp.body().asInputStream(), StandardCharsets.UTF_8);
    }

    @Nullable
    public static String firstHeader(Response resp, String header) {
        var values = resp.headers().get(header);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.iterator().next();
    }

    @Nullable
    public static Type getFirstTypeArgumentIfMatch(Type type, Class<?> expectType) {
        if (!(type instanceof ParameterizedType parameterized)) {
            return null;
        }
        if (expectType != parameterized.getRawType()) {
            return null;
        }
        return parameterized.getActualTypeArguments()[0];
    }
}
