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

import java.util.List;
import java.util.Map;

import static org.febit.boot.jooq.converter.Pojos.F1;
import static org.febit.boot.jooq.converter.Pojos.F2000;
import static org.febit.lang.util.JacksonUtils.toJsonString;
import static org.junit.jupiter.api.Assertions.*;

class JsonStringConverterTest {

    @Test
    void forBean() {
        var c = JsonStringConverter.forBean(Pojos.Foo.class);
        assertEquals(Pojos.Foo.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(F2000, c.from(
                toJsonString(F2000)
        ));
        assertEquals(F1, c.from(c.to(F1)));
    }

    @Test
    void forBeanList() {
        var c = JsonStringConverter.forBeanList(Pojos.Foo.class);
        assertEquals(List.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertIterableEquals(List.of(), c.from("[]"));

        assertIterableEquals(List.of(F2000), c.from(
                toJsonString(List.of(F2000))
        ));
        assertIterableEquals(List.of(F1, F2000, F2000, F1),
                c.from(c.to(List.of(F1, F2000, F2000, F1)))
        );
    }

    @Test
    void forBeanArray() {
        var c = JsonStringConverter.forBeanArray(Pojos.Foo.class);
        assertEquals(Pojos.Foo[].class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertArrayEquals(new Pojos.Foo[0], c.from("[]"));
    }

    @Test
    void forBeanMap() {
        var c = JsonStringConverter.forBeanMap(String.class, Pojos.Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.of(), c.from("{}"));

        assertEquals(Map.of("a", F2000), c.from(
                toJsonString(Map.of("a", F2000))
        ));
        assertEquals(Map.of("a", F1, "b", F2000, "c", F2000, "d", F1),
                c.from(c.to(Map.of("a", F1, "b", F2000, "c", F2000, "d", F1)))
        );
    }

}
