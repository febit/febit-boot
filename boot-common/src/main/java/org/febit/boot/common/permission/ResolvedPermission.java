package org.febit.boot.common.permission;

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
