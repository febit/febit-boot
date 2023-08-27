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

import com.fasterxml.jackson.core.type.ResolvedType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExtraEnumMetaPropertyCustomizer implements PropertyCustomizer {

    private static final String EX_ENUM_FULL_CLASS = "x-enum-full-class";
    private static final String EX_ENUM_CLASS = "x-enum-class";

    @Override
    @SuppressWarnings("rawtypes")
    public Schema customize(Schema schema, AnnotatedType type) {
        var enums = schema.getEnum();
        if (CollectionUtils.isEmpty(enums)) {
            return schema;
        }
        var enumType = type.getType();

        Class<?> cls = null;
        if (enumType instanceof Class) {
            cls = ((Class<?>) enumType);
        } else if (enumType instanceof ResolvedType) {
            cls = ((ResolvedType) enumType).getRawClass();
        }

        if (cls != null) {
            schema.addExtension(EX_ENUM_FULL_CLASS, cls.getCanonicalName());
            schema.addExtension(EX_ENUM_CLASS, cls.getSimpleName());
        } else {
            schema.addExtension(EX_ENUM_FULL_CLASS, enumType.getTypeName());
        }

        return schema;
    }
}
