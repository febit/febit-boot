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

import feign.QueryMapEncoder;
import lombok.RequiredArgsConstructor;
import org.febit.lang.util.JacksonWrapper;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class JacksonQueryMapEncoder implements QueryMapEncoder {

    private final JacksonWrapper jackson;

    @Override
    public Map<String, Object> encode(Object form) {
        if (form == null) {
            return Collections.emptyMap();
        }
        return jackson.toNamedMap(form);
    }
}
