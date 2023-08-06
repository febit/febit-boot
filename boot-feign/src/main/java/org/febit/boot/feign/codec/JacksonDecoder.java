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
package org.febit.boot.feign.codec;

import feign.Response;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.feign.FeignUtils;
import org.febit.lang.util.JacksonWrapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;

@RequiredArgsConstructor
public class JacksonDecoder implements Decoder {

    private final JacksonWrapper jackson;

    @Override
    @Nullable
    public Object decode(Response response, Type type) throws IOException {
        var body = FeignUtils.readUtf8(response);
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        return this.jackson.parse(body, type);
    }

}
