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
package org.febit.boot.jooq.converter;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InstantConverterTest {

    @Test
    void test() {
        var converter = new InstantConverter();

        assertEquals(Timestamp.class, converter.fromType());
        assertEquals(Instant.class, converter.toType());

        assertNull(converter.from(null));
        assertNull(converter.to(null));

        var timestamp = Timestamp.from(Instant.now());

        assertEquals(timestamp.toInstant(), converter.from(timestamp));
        assertEquals(timestamp, converter.to(converter.from(timestamp)));
    }

}
