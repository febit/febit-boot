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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "create")
public class ClassWriter {

    private final StringBuilder buf = new StringBuilder();

    @Getter
    private final ImportSet imports;

    @Getter
    private final String fullName;

    public static ClassWriter create(ClassNaming naming, String fullName) {
        var imports = new ImportSet(naming);
        return create(imports, fullName);
    }

    public ClassWriter tab(int i) {
        return append("    ".repeat(i));
    }

    public ClassWriter appendDirectValue(String raw) {
        return append(CodeUtils.toCodeString(raw));
    }

    public ClassWriter appendClassHeader() {
        var pkg = CodeUtils.pkg(fullName);
        buf.append("package ").append(pkg).append(";\n")
                .append("\n");

        imports.emit(buf, pkg);
        return this;
    }

    public ClassWriter appendDeclare(String type) {
        buf.append("public ")
                .append(type)
                .append(" ")
                .append(CodeUtils.classSimpleName(fullName))
                .append(" {\n\n");
        return this;
    }

    public ClassWriter append(String code) {
        buf.append(code);
        return this;
    }

    public <T> ClassWriter foreach(List<T> items, String split, Consumer<T> consumer) {
        boolean first = true;
        for (T item : items) {
            if (!first) {
                buf.append(split);
            } else {
                first = false;
            }
            consumer.accept(item);
        }
        return this;
    }

    public <T> ClassWriter foreach(List<T> items, String split, String last, Consumer<T> consumer) {
        boolean first = true;
        for (T item : items) {
            if (!first) {
                buf.append(split);
            } else {
                first = false;
            }
            consumer.accept(item);
        }
        if (!first) {
            buf.append(last);
        }
        return this;
    }

    public ClassWriter append(char c) {
        buf.append(c);
        return this;
    }

    public ClassWriter appendType(ResolvableType type) {
        return append(imports.resolveTypeEmitName(type));
    }

    public ClassWriter appendClass(String type) {
        return append(imports.resolveClassEmitName(type));
    }

    public ClassWriter appendFooter() {
        buf.append("\n");
        buf.append("}\n");
        return this;
    }

    public void closeAndSinkTo(File targetDir) {
        appendFooter();
        sink(targetDir);
    }

    public void sink(File targetDir) {
        var pkg = CodeUtils.pkg(fullName);
        var name = CodeUtils.classSimpleName(fullName);

        try {
            var folder = new File(targetDir, pkg.replace('.', '/'));
            var classFile = new File(folder, name + ".java");
            FileUtils.forceMkdir(folder);
            FileUtils.write(classFile, buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
