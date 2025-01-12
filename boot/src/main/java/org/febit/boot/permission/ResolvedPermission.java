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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor(
        access = AccessLevel.PRIVATE,
        staticName = "of"
)
public class ResolvedPermission {

    public static final ResolvedPermission IGNORED
            = of(Type.IGNORED, "Ignored", List.of());

    public static final ResolvedPermission ANONYMOUS
            = of(Type.IGNORED, "Anonymous", List.of());

    public static final ResolvedPermission FORBIDDEN_ABSENT
            = of(Type.FORBIDDEN, "Forbidden: permission is absent.", List.of());

    public static final ResolvedPermission FORBIDDEN_NOT_IN_CHARGE
            = of(Type.FORBIDDEN, "Forbidden: not in charge.", List.of());

    private final Type type;
    private final String message;
    private final List<PermissionItem> items;

    public boolean isIgnored() {
        return type == Type.IGNORED;
    }

    public boolean isForbidden() {
        return type == Type.FORBIDDEN;
    }

    public boolean isAllowList() {
        return type == Type.ALLOW_LIST;
    }

    public int getItemsSize() {
        return this.items.size();
    }

    public enum Type {
        IGNORED,
        FORBIDDEN,
        ALLOW_LIST,
    }

    public static ResolvedPermission allow(Collection<PermissionItem> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            throw new IllegalArgumentException("permission list is empty.");
        }
        var sorted = permissions.size() == 1
                ? List.copyOf(permissions)
                : permissions.stream().sorted().toList();

        return of(Type.ALLOW_LIST, "Allow permissions", sorted);
    }
}
