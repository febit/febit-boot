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
