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
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import lombok.experimental.UtilityClass;
import org.febit.boot.feign.codec.JacksonDecoder;
import org.febit.boot.feign.codec.ResponseStatusDecoder;
import org.febit.boot.feign.codec.StandardErrorDecoder;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;

@UtilityClass
public class Decoders {

    public static Decoder chain(
            ObjectMapper objectMapper,
            Chain... chains
    ) {
        Decoder decoder = jackson(objectMapper);
        for (var chain : chains) {
            decoder = chain.chain(decoder);
        }
        return decoder;
    }

    public static Decoder jackson(ObjectMapper objectMapper) {
        return jackson(JacksonUtils.wrap(objectMapper));
    }

    public static Decoder jackson(JacksonWrapper wrapper) {
        return new JacksonDecoder(wrapper);
    }

    public static StandardErrorDecoder standardErrorDecoder() {
        return new StandardErrorDecoder();
    }

    public static ResponseStatusDecoder responseStatusDecoder(Decoder delegate) {
        return new ResponseStatusDecoder(delegate);
    }

    public static OptionalDecoder optionalDecoder(Decoder delegate) {
        return new OptionalDecoder(delegate);
    }

    @FunctionalInterface
    public interface Chain {
        Decoder chain(Decoder inner);
    }

}
