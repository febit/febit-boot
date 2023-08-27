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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.common.permission.PermissionItem;
import org.febit.boot.common.permission.PermissionManager;
import org.febit.boot.common.permission.ResolvedPermission;
import org.febit.lang.util.Lists;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

@Slf4j
@Component
@ConditionalOnClass({
        GlobalOperationCustomizer.class
})
@ConditionalOnBean({
        PermissionManager.class
})
@RequiredArgsConstructor
public class PermissionOperationCustomizer implements GlobalOperationCustomizer {

    public static final String X_PERMISSION = "x-permission";

    private final PermissionManager permissionManager;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        var meta = permissionManager.getPermission(handlerMethod.getMethod());
        operation.addExtension(X_PERMISSION, PermissionDocMeta.of(meta));
        return operation;
    }

    @Getter
    @Setter
    public static class PermissionDocMeta {

        private ResolvedPermission.Type type;
        private List<String> items;

        public static PermissionDocMeta of(ResolvedPermission src) {
            var meta = new PermissionDocMeta();
            meta.setType(src.getType());
            meta.setItems(Lists.collect(src.getItems(), PermissionItem::getCode));
            return meta;
        }
    }
}
