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

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;
import org.febit.lang.util.JacksonWrapper;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JacksonEncoder implements Encoder {

    private final JacksonWrapper jackson;

    @Override
    public void encode(@Nullable Object body, Type bodyType, RequestTemplate template) throws EncodeException {
        if (body == null) {
            template.body("");
            return;
        }
        var out = new ByteArrayOutputStream();
        try {
            this.jackson.writeTo(out, body);
        } catch (IOException e) {
            throw new EncodeException("IOException: " + e.getMessage(), e);
        }
        template.body(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
