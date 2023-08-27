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

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.web.util.SpringdocUtils;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExtraMetaPropertyCustomizer implements PropertyCustomizer {

    private static final String EX_CLASS = "x-class";

    @Override
    @SuppressWarnings("rawtypes")
    public Schema customize(Schema schema, AnnotatedType annotatedType) {
        var type = SpringdocUtils.extractRawClass(annotatedType);
        if (type == null) {
            return schema;
        }
        schema.addExtension(EX_CLASS, type.getSimpleName());
        return schema;
    }
}
