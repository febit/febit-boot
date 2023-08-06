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

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.QueryMapEncoder;
import feign.codec.Encoder;
import lombok.experimental.UtilityClass;
import org.febit.boot.feign.codec.JacksonEncoder;
import org.febit.boot.feign.codec.JacksonQueryMapEncoder;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;

@UtilityClass
public class Encoders {

    public static Encoder chain(
            ObjectMapper objectMapper,
            Chain... chains
    ) {
        var encoder = jackson(objectMapper);
        for (var action : chains) {
            encoder = action.chain(encoder);
        }
        return encoder;
    }

    public static Encoder jackson(ObjectMapper objectMapper) {
        return jackson(JacksonUtils.wrap(objectMapper));
    }

    public static Encoder jackson(JacksonWrapper jackson) {
        return new JacksonEncoder(jackson);
    }

    public static QueryMapEncoder queryMapEncoder(JacksonWrapper jackson) {
        return new JacksonQueryMapEncoder(jackson);
    }

    @FunctionalInterface
    public interface Chain {
        Encoder chain(Encoder inner);
    }

}
