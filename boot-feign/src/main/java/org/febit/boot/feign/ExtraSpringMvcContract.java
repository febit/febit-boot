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
package org.febit.boot.feign;

import feign.MethodMetadata;
import jakarta.annotation.Nullable;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ExtraSpringMvcContract extends SpringMvcContract {

    private final Map<MethodMetadata, Method> metaToMethodMapping = new IdentityHashMap<>();
    private final Set<Class<?>> ignoreParameterTypes;
    private final boolean decodeSplash;

    @lombok.Builder(builderClassName = "Builder")
    protected ExtraSpringMvcContract(
            @Singular List<AnnotatedParameterProcessor> annotatedParameterProcessors,
            @Nullable Boolean decodeSplash,
            @Nullable ConversionService conversionService,
            @Singular Set<Class<?>> ignoreParameterTypes
    ) {
        super(
                annotatedParameterProcessors,
                conversionService != null ? conversionService
                        : new DefaultConversionService()
        );
        this.decodeSplash = decodeSplash != null && decodeSplash;
        this.ignoreParameterTypes = ignoreParameterTypes;
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
        this.metaToMethodMapping.put(data, method);

        // FIXED: NPE of uri when set decodeSplash,
        data.template().uri("", true);
        data.template().decodeSlash(this.decodeSplash);

        super.processAnnotationOnMethod(data, methodAnnotation, method);
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {

        // Ignore by ApiIgnore
        for (var anno : annotations) {
            if (anno instanceof ApiIgnore) {
                return true;
            }
        }

        // Ignore if in ignore list
        var method = this.metaToMethodMapping.get(data);
        var paramType = method.getParameterTypes()[paramIndex];
        for (Class<?> type : this.ignoreParameterTypes) {
            if (type.isAssignableFrom(paramType)) {
                return true;
            }
        }

        return super.processAnnotationsOnParameter(data, annotations, paramIndex);
    }
}
