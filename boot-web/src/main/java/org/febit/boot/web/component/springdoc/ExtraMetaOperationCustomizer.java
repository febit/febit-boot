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

import io.swagger.v3.oas.models.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Slf4j
@Component
@ConditionalOnClass({
        GlobalOperationCustomizer.class
})
@RequiredArgsConstructor
public class ExtraMetaOperationCustomizer implements GlobalOperationCustomizer {

    private static final String X_METHOD_NAME = "x-method-name";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        var method = handlerMethod.getMethod();
        operation.addExtension(X_METHOD_NAME, method.getName());
        return operation;
    }
}
