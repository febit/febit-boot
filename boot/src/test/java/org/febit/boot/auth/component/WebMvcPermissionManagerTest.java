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
package org.febit.boot.auth.component;

import org.febit.boot.permission.AnnotatedMethodPermissionResolver;
import org.febit.boot.permission.PermissionItem;
import org.febit.boot.permission.ResolvedPermission;
import org.junit.jupiter.api.Test;
import org.springdoc.SampleDocController;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebMvcPermissionManagerTest {

    @Test
    void lifecycleWithoutHandlerMappings() {
        var manager = managerWith(Map.of());
        assertThat(manager.isRunning()).isFalse();
        assertThat(manager.getAllPermissionItems()).isEmpty();
        assertThat(manager.getPermission(anyMethod())).isSameAs(ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE);

        manager.start();

        assertThat(manager.isRunning()).isTrue();
        assertThat(manager.getAllPermissionItems()).isEmpty();
        assertThat(manager.getPhase()).isEqualTo(Integer.MAX_VALUE - 10);

        manager.stop();

        assertThat(manager.isRunning()).isFalse();
        assertThat(manager.getAllPermissionItems()).isEmpty();
    }

    @Test
    void collectsPermissionsFromHandlerMethodsOnStart() throws Exception {
        var manager = managerWith(handlers(new SampleController(), "data"));
        manager.start();

        assertThat(manager.getAllPermissionItems())
                .extracting(PermissionItem::code)
                .contains("res");
        assertThat(manager.getPermission(SampleController.class.getDeclaredMethod("data")).isAllowList()).isTrue();
    }

    @Test
    void collectsDistinctSortedItems() throws Exception {
        var m1 = SampleController.class.getDeclaredMethod("data");
        var m2 = OtherController.class.getDeclaredMethod("other");
        var manager = managerWith(Map.of(
                RequestMappingInfo.paths("/a").build(), new HandlerMethod(new SampleController(), m1),
                RequestMappingInfo.paths("/b").build(), new HandlerMethod(new SampleController(), m1),
                RequestMappingInfo.paths("/c").build(), new HandlerMethod(new OtherController(), m2)
        ));
        manager.start();

        assertThat(manager.getAllPermissionItems())
                .extracting(PermissionItem::code)
                .containsExactly("other", "res");
    }

    @Test
    void ignoresHandlerMethodsInExcludedPackages() throws Exception {
        var method = SampleDocController.class.getDeclaredMethod("apiDocs");
        var manager = managerWith(Map.of(
                RequestMappingInfo.paths("/doc").build(),
                new HandlerMethod(new SampleDocController(), method)
        ));
        manager.start();

        assertThat(manager.getPermission(method).isIgnored()).isTrue();
    }

    @Test
    void stopClearsAndRestartRebuilds() throws Exception {
        var method = SampleController.class.getDeclaredMethod("data");
        var manager = managerWith(handlers(new SampleController(), "data"));
        manager.start();
        assertThat(manager.getPermission(method).isAllowList()).isTrue();

        manager.stop();
        assertThat(manager.isRunning()).isFalse();
        assertThat(manager.getAllPermissionItems()).isEmpty();
        assertThat(manager.getPermission(method)).isSameAs(ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE);

        manager.start();
        assertThat(manager.isRunning()).isTrue();
        assertThat(manager.getPermission(method).isAllowList()).isTrue();
    }

    @Test
    void secondStartIsNoOp() {
        var manager = managerWith(Map.of());
        manager.start();
        assertThat(manager.isRunning()).isTrue();

        // re-entering start() must be a no-op (idempotent)
        manager.start();
        assertThat(manager.isRunning()).isTrue();
    }

    private static WebMvcPermissionManager managerWith(Map<RequestMappingInfo, HandlerMethod> handlers) {
        var app = mock(ApplicationContext.class);
        var mapping = mock(RequestMappingInfoHandlerMapping.class);
        when(mapping.getHandlerMethods()).thenReturn(handlers);
        when(app.getBeansOfType(RequestMappingInfoHandlerMapping.class)).thenReturn(Map.of("m", mapping));
        return new WebMvcPermissionManager(app, List.of(new AnnotatedMethodPermissionResolver()));
    }

    private static Map<RequestMappingInfo, HandlerMethod> handlers(Object controller, String methodName)
            throws Exception {
        var method = controller.getClass().getDeclaredMethod(methodName);
        return Map.of(RequestMappingInfo.paths("/x").build(), new HandlerMethod(controller, method));
    }

    private static Method anyMethod() {
        try {
            return String.class.getDeclaredMethod("length");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    static class SampleController {
        @org.febit.boot.permission.Permission(resource = "res")
        public void data() {
        }
    }

    @SuppressWarnings("unused")
    static class OtherController {
        @org.febit.boot.permission.Permission(resource = "other")
        public void other() {
        }
    }
}
