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
