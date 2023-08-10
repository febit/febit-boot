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
