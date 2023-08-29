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
