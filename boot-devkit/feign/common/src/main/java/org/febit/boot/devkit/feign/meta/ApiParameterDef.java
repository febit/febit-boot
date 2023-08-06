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
package org.febit.boot.devkit.feign.meta;

import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.febit.boot.devkit.feign.util.CodeUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Parameter;

@Data
@Builder(builderClassName = "Builder")
public class ApiParameterDef {

    private final String name;
    private final ResolvableType type;
    private final In in;
    private final String bindName;
    private final Boolean isDeprecated;

    public static ApiParameterDef of(Parameter param) {
        val type = ResolvableType.forMethodParameter(
                MethodParameter.forParameter(param)
        );

        val builder = ApiParameterDef.builder()
                .type(type)
                .name(param.getName())
                .isDeprecated(CodeUtils.isDeprecated(param));

        val bodyAnno = AnnotatedElementUtils.findMergedAnnotation(param, RequestBody.class);
        val pathVarAnno = AnnotatedElementUtils.findMergedAnnotation(param, PathVariable.class);
        val queryAnno = AnnotatedElementUtils.findMergedAnnotation(param, RequestParam.class);
        val headerAnno = AnnotatedElementUtils.findMergedAnnotation(param, RequestHeader.class);
        val modelAttrAnno = AnnotatedElementUtils.findMergedAnnotation(param, ModelAttribute.class);

        if (pathVarAnno != null) {
            builder.in(In.PATH_VAR)
                    .bindName(pathVarAnno.name());
        } else if (headerAnno != null) {
            builder.in(In.HEADER)
                    .bindName(headerAnno.name());
        } else if (queryAnno != null) {
            builder.in(In.QUERY)
                    .bindName(queryAnno.name());
        } else if (modelAttrAnno != null) {
            builder.in(In.MODEL_ATTR);
        } else if (bodyAnno != null) {
            builder.in(In.BODY);
        } else {
            // NOTE: default is query-map in SpringMVC
            builder.in(In.QUERY_MAP);
        }

        return builder.build();
    }

    public enum In {
        QUERY,
        QUERY_MAP,
        PATH_VAR,
        MODEL_ATTR,
        HEADER,
        BODY,
        ;
    }
}
