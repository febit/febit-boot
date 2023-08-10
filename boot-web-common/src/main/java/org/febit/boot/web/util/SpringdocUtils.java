package org.febit.boot.web.util;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.util.Json;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
public class SpringdocUtils {

    @Nullable
    public static Class<?> extractRawClass(AnnotatedType annotatedType) {
        JavaType javaType = Json.mapper().constructType(annotatedType.getType());
        if (javaType == null) {
            return null;
        }
        return javaType.getRawClass();
    }

}
