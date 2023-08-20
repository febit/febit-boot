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
package org.febit.boot.devkit.feign.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.febit.devkit.gradle.util.JavaUtils;
import org.febit.lang.util.Lists;
import org.springframework.core.ResolvableType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ImportSet {

    public static final String PKG_JAVA_LANG = "java.lang";
    private final Set<String> imported = new HashSet<>();
    private final Set<ResolvableType> resolvedTypes = new HashSet<>();

    private final ClassNaming classNaming;

    public void emit(StringBuilder buf, String currentPkg) {
        var count = new AtomicInteger(0);
        imported.stream()
                .filter(cls -> !JavaUtils.isInPackage(cls, PKG_JAVA_LANG))
                .filter(cls -> !JavaUtils.isInPackage(cls, currentPkg))
                .sorted()
                .forEach(cls -> {
                    count.incrementAndGet();
                    buf.append("import ")
                            .append(cls).append(";\n");
                });
        if (count.get() > 0) {
            buf.append("\n");
        }
    }

    public void add(ResolvableType type) {
        if (resolvedTypes.contains(type)) {
            return;
        }
        resolvedTypes.add(type);
        var cls = type.resolve(Object.class);
        add(cls);
        Stream.of(type.getGenerics())
                .forEach(this::add);
    }

    public void add(Class<?> cls) {
        if (cls.isPrimitive()) {
            return;
        }
        if (cls.isArray()) {
            add(JavaUtils.resolveFinalComponentType(cls.getComponentType()));
            return;
        }
        transferAndAdd(cls.getName());
    }

    public void add(String classFullName) {
        imported.add(classFullName);
    }

    public boolean contains(String classFullName) {
        return imported.contains(classFullName);
    }

    public boolean contains(Class<?> cls) {
        if (cls.isPrimitive()) {
            return true;
        }
        if (cls.isArray()) {
            return contains(JavaUtils.resolveFinalComponentType(cls.getComponentType()));
        }
        return contains(transfer(cls));
    }

    public String transfer(Class<?> cls) {
        return transfer(cls.getName());
    }

    public String transfer(String classFullName) {
        return classNaming.resolve(classFullName);
    }

    private void transferAndAdd(String classFullName) {
        add(transfer(classFullName));
    }

    public String resolveClassEmitName(String cls) {
        return contains(cls)
                ? StringUtils.substringAfterLast(cls, ".")
                : cls;
    }

    public String resolveClassEmitName(Class<?> cls) {
        if (cls.isArray()) {
            return resolveClassEmitName(cls.getComponentType())
                    + "[]";
        }
        var full = transfer(cls);
        if (cls.isPrimitive()) {
            return full;
        }
        return contains(cls)
                ? JavaUtils.classSimpleName(full)
                : full;
    }

    public String resolveTypeEmitName(ResolvableType type) {
        if (type.isArray()) {
            return resolveTypeEmitName(type.getComponentType())
                    + "[]";
        }
        var cls = type.resolve(Object.class);
        var main = resolveClassEmitName(cls);

        var params = type.getGenerics();
        if (params.length == 0) {
            return main;
        }
        if ("?".equals(type.toString())) {
            return "?";
        }

        return main + '<' + StringUtils.join(
                resolveTypeEmitNames(params),
                ", "
        ) + '>';
    }

    public List<String> resolveTypeEmitNames(ResolvableType... types) {
        return Lists.collect(types, this::resolveTypeEmitName);
    }
}
