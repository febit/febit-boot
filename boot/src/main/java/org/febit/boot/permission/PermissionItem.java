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
package org.febit.boot.permission;

import java.util.Comparator;

public record PermissionItem(
        String code,
        String module,
        String resource,
        String action
) implements Comparable<PermissionItem> {

    private static final Comparator<PermissionItem> COMPARATOR
            = Comparator.comparing(PermissionItem::module)
            .thenComparing(PermissionItem::resource)
            .thenComparing(PermissionItem::action);

    public static PermissionItem of(Permission permission) {
        var module = permission.module().toLowerCase();
        var resource = permission.resource().toLowerCase();
        var action = permission.action().toLowerCase();
        var separator = permission.separator();

        String code;
        if (module.isEmpty() && action.isEmpty()) {
            code = resource;
        } else {
            var buf = new StringBuilder();
            if (!module.isEmpty()) {
                buf.append(module);
                buf.append(separator);
            }
            buf.append(resource);
            if (!action.isEmpty()) {
                buf.append(separator);
                buf.append(action);
            }
            code = buf.toString();
        }
        return new PermissionItem(code, module, resource, action);
    }

    @Override
    public int compareTo(PermissionItem other) {
        return COMPARATOR.compare(this, other);
    }

}
