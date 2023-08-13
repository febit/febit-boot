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
import org.apache.commons.lang3.ArrayUtils;
import org.febit.boot.jooq.Column;
import org.febit.boot.jooq.SearchForm;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({
        SearchForm.class,
        PropertyCustomizer.class
})
public class JooqSearchFormPropertyCustomizer implements PropertyCustomizer {

    private static final String EX_SEARCH_FILTER = "x-search-filter";

    @Override
    public Schema customize(Schema schema, AnnotatedType annotatedType) {
        var annotations = annotatedType.getCtxAnnotations();
        if (ArrayUtils.isEmpty(annotations)) {
            return schema;
        }
        var colAnno = AnnotatedElementUtils.findMergedAnnotation(
                AnnotatedElementUtils.forAnnotations(annotatedType.getCtxAnnotations()),
                Column.class
        );
        if (colAnno == null) {
            return schema;
        }
        schema.addExtension(EX_SEARCH_FILTER, colAnno.operator().name());
        return schema;
    }
}
