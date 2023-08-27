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
package org.febit.boot.web.component.springdoc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.febit.boot.web.component.springdoc.GenericTypeNameResolver.INSTANCE;
import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;
import static org.junit.jupiter.api.Assertions.*;

class GenericTypeNameResolverTest {

    @Test
    void nameForGenericType() {

        assertEquals("String",
                INSTANCE.nameForGenericType(
                        TYPE_FACTORY.constructType(String.class),
                        Set.of()
                )
        );

        assertEquals("Boolean",
                INSTANCE.nameForGenericType(
                        TYPE_FACTORY.constructType(Boolean.class),
                        Set.of()
                )
        );

        assertEquals("Map<String,List<String>>",
                INSTANCE.nameForGenericType(TYPE_FACTORY.constructMapType(
                        Map.class,
                        TYPE_FACTORY.constructType(String.class),
                        TYPE_FACTORY.constructCollectionType(List.class, String.class)
                ), Set.of())
        );
    }
}
