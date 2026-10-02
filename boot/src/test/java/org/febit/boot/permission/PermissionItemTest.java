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

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionItemTest {

    @TableTest("""
            module | resource | action | separator | expectedCode
                   | r        |        | :         | r
            m      | r        |        | :         | m:r
                   | r        | a      | :         | r:a
            m      | r        | a      | :         | m:r:a
            m      | r        | a      | /         | m/r/a
            M      | R        | A      | :         | m:r:a
        """)
    void buildsCode(String module, String resource, String action, String separator, String expectedCode) {
        var permission = permissionOf(module, resource, action, separator);
        assertThat(PermissionItem.of(permission).code()).isEqualTo(expectedCode);
    }

    private static Permission permissionOf(String module, String resource, String action, String separator) {
        return (Permission) Proxy.newProxyInstance(
                Permission.class.getClassLoader(),
                new Class<?>[]{Permission.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "module" -> module == null ? "" : module;
                    case "resource" -> resource == null ? "" : resource;
                    case "action" -> action == null ? "" : action;
                    case "separator" -> separator == null ? ":" : separator;
                    case "annotationType" -> Permission.class;
                    case "toString" -> "@Permission";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    void ordersByModuleResourceAction() {
        var a = new PermissionItem("a:x", "a", "x", "x");
        var b = new PermissionItem("b:x", "b", "x", "x");
        var c = new PermissionItem("a:y", "a", "y", "x");
        assertThat(a).isLessThan(b);
        assertThat(a).isLessThan(c);
        assertThat(c).isLessThan(b);
    }

    @Test
    void equalItemsCompareEqual() {
        var a = new PermissionItem("m:r:a", "m", "r", "a");
        var b = new PermissionItem("m:r:a", "m", "r", "a");
        assertThat(a).isEqualByComparingTo(b);
        assertThat(a).isEqualTo(b);
    }
}
