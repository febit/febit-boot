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
package org.febit.boot.web.component.mvc;

import org.febit.boot.jooq.SearchForm;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@ControllerAdvice
@ConditionalOnClass({
        SearchForm.class,
        RequestBodyAdvice.class,
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EmptySearchFormRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        if (!(targetType instanceof Class<?> cls)) {
            return false;
        }
        return SearchForm.class.isAssignableFrom(cls);
    }

    @Nullable
    @Override
    public Object handleEmptyBody(
            @Nullable Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        if (body != null) {
            return body;
        }
        if (!(targetType instanceof Class<?> cls)) {
            return null;
        }
        return BeanUtils.instantiateClass(cls);
    }
}
