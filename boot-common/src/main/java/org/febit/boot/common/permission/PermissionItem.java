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
package org.febit.boot.common.permission;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
        staticName = "of"
)
public class PermissionItem implements Comparable<PermissionItem> {

    private final String code;

    public static PermissionItem of(Permission permission) {

        var module = permission.module();
        var action = permission.action();
        var code = permission.code();

        if (module.isEmpty()
                && action.isEmpty()
        ) {
            return of(code);
        }

        var separator = permission.separator();
        var buf = new StringBuilder();
        if (!module.isEmpty()) {
            buf.append(module);
            buf.append(separator);
        }
        buf.append(code);

        if (!action.isEmpty()) {
            buf.append(separator);
            buf.append(action);
        }
        return of(buf.toString());
    }

    @Override
    public int compareTo(PermissionItem meta) {
        return this.code.compareTo(meta.code);
    }
}
