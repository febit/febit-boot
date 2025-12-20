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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.febit.boot.permission.MethodPermissionResolver;
import org.febit.boot.permission.MethodPermissionResolvers;
import org.febit.boot.permission.PermissionItem;
import org.febit.boot.permission.PermissionManager;
import org.febit.boot.permission.ResolvedPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RequestMappingInfoHandlerMapping.class)
@ConditionalOnWebApplication(
        type = ConditionalOnWebApplication.Type.SERVLET
)
public class WebMvcPermissionManager implements SmartLifecycle, PermissionManager {

    private static final String[] IGNORED_PACKAGES = {
            // springdoc
            "org.springdoc.",
            // actuate
            "org.springframework.boot.actuate.endpoint.web.servlet.",
            // build-in errors
            "org.springframework.boot.autoconfigure.web.servlet.error.",
    };

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<Method, ResolvedPermission> mapping = new ConcurrentHashMap<>(256);
    private final AtomicReference<List<PermissionItem>> itemsRef = new AtomicReference<>(List.of());

    private final ApplicationContext app;
    private final List<MethodPermissionResolver> resolvers;

    @Override
    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        this.app.getBeansOfType(RequestMappingInfoHandlerMapping.class)
                .values()
                .forEach(m ->
                        m.getHandlerMethods().forEach(this::collect)
                );
        this.itemsRef.set(this.mapping.values().stream()
                .flatMap(meta -> meta.getItems().stream())
                .distinct()
                .sorted()
                .toList()
        );
        summary();
    }

    private void summary() {
        if (!log.isInfoEnabled()) {
            return;
        }
        var ignored = this.mapping.values().stream()
                .filter(ResolvedPermission::isIgnored)
                .count();
        var forbidden = this.mapping.values().stream()
                .filter(ResolvedPermission::isForbidden)
                .count();
        var itemsSize = this.mapping.values().stream()
                .mapToInt(ResolvedPermission::getItemsSize)
                .sum();

        var unique = this.itemsRef.get().size();
        var methods = this.mapping.size();
        log.info("Found [{}] permissions on [{}] methods, unique [{}], ignored [{}], forbidden [{}].",
                itemsSize, methods, unique, ignored, forbidden
        );
    }

    @Override
    public synchronized void stop() {
        this.mapping.clear();
        this.itemsRef.set(List.of());
        this.started.getAndSet(false);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public int getPhase() {
        // Should before than Swagger
        return Integer.MAX_VALUE - 10;
    }

    @Override
    public ResolvedPermission getPermission(Method method) {
        return mapping.getOrDefault(method,
                ResolvedPermission.FORBIDDEN_NOT_IN_CHARGE);
    }

    @Override
    public List<PermissionItem> getAllPermissionItems() {
        return itemsRef.get();
    }

    private void collect(RequestMappingInfo mapping, HandlerMethod handlerMethod) {
        var method = handlerMethod.getMethod();
        if (isIgnored(handlerMethod)) {
            log.info("Ignored permission on method [{}#{}(...)], mapping [{}]", method.getDeclaringClass(), method.getName(), mapping);
            this.mapping.put(method, ResolvedPermission.IGNORED);
            return;
        }
        this.mapping.put(method,
                MethodPermissionResolvers.resolve(resolvers, method)
        );
    }

    private boolean isIgnored(HandlerMethod handlerMethod) {
        var className = handlerMethod.getMethod().getDeclaringClass().getName();
        return Strings.CS.startsWithAny(className, IGNORED_PACKAGES);
    }

}
