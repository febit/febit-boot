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
package org.febit.boot.devkit.feign;

import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.febit.boot.devkit.feign.meta.ClientDef;
import org.febit.boot.devkit.feign.util.ClassNaming;
import org.febit.boot.devkit.feign.util.ClassWriter;
import org.febit.boot.devkit.feign.util.CodeUtils;
import org.febit.devkit.gradle.util.JavaUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Builder(builderClassName = "Builder")
public class ClientCodegen {

    final String clientBasePackage;
    final String clientName;
    final String clientUrl;

    final ClassNaming pojoNaming;
    final ClassNaming clientNaming;

    @Singular
    final List<String> clientConfigClasses;
    @Singular
    final Set<String> excludedClasses;

    final File targetDir;

    private final List<ClientDef> clients;
    private final Map<String, BeanDefinition> beans;

    private final Set<Object> emittedTypes = new HashSet<>();

    public void emit() {
        clients.forEach(client -> new ClientEmitter(this, client).emit());
    }

    void emitPojoIfAbsent(ResolvableType type) {
        if (emittedTypes.contains(type)) {
            return;
        }
        emittedTypes.add(type);

        Stream.of(type.getGenerics())
                .forEach(this::emitPojoIfAbsent);

        val cls = type.resolve();
        if (cls == null) {
            log.info("Unresolvable type: {}", type);
            return;
        }
        val componentClass = CodeUtils.resolveFinalComponentType(cls);
        if (emittedTypes.contains(componentClass)) {
            return;
        }
        emittedTypes.add(componentClass);

        if (excludedClasses.contains(componentClass.getName())) {
            return;
        }

        if (!pojoNaming.resolve(componentClass.getName())
                .startsWith(clientBasePackage)) {
            return;
        }

        if (componentClass.isEnum()) {
            @SuppressWarnings("unchecked")
            val enumType = (Class<? extends Enum<?>>) componentClass;
            emitEnum(enumType);
            return;
        }

        emitPojo(componentClass);
    }

    private void emitEnum(Class<? extends Enum<?>> cls) {

        val fullName = pojoNaming.resolve(cls.getName());
        if (excludedClasses.contains(fullName)) {
            return;
        }

        log.info("Emit enum: {}", cls);
        val out = ClassWriter.create(pojoNaming, fullName);
        out.appendClassHeader();
        out.appendDeclare("enum");

        for (val e : cls.getEnumConstants()) {
            out.tab(1).append("")
                    .append(e.name())
                    .append(",\n");
        }

        out.tab(1).append(";\n");

        out.closeAndSinkTo(this.targetDir);
    }

    private void emitPojo(Class<?> cls) {
        val fullName = pojoNaming.resolve(cls.getName());
        if (excludedClasses.contains(fullName)) {
            return;
        }

        log.info("Emit POJO: {} <= {}", fullName, cls.getName());
        val out = ClassWriter.create(pojoNaming, fullName);
        out.appendClassHeader();
        out.appendDeclare("class");

        val props = BeanUtils.getPropertyDescriptors(cls);

        for (val prop : props) {
            val type = CodeUtils.getPropertyResolvableType(prop);
            out.getImports().add(type);
            emitPojoIfAbsent(type);
        }

        out.appendClassHeader();

        if (CodeUtils.isDeprecated(cls)) {
            out.append("@Deprecated\n");
        }
        out.append("@lombok.Data\n"
                + "@lombok.extern.jackson.Jacksonized\n"
                + "@lombok.NoArgsConstructor\n"
                + "@lombok.AllArgsConstructor(staticName = \"of\")\n"
                + "@lombok.Builder(builderClassName = \"Builder\")\n"
        );
        out.appendDeclare("class");

        for (val prop : props) {
            if ("class".equals(prop.getName())) {
                continue;
            }

            if (CodeUtils.isDeprecated(prop)) {
                out.tab(1).append("@Deprecated\n");
            }

            val type = CodeUtils.getPropertyResolvableType(prop);
            val isKeyword = JavaUtils.isKeyword(prop.getName());

            if (isKeyword) {
                out.tab(1).append("@com.fasterxml.jackson.annotation.JsonProperty(\"")
                        .append(prop.getName())
                        .append("\")\n");
            }

            out.tab(1).append("private ")
                    .appendType(type)
                    .append(" ")
                    .append(isKeyword
                            ? prop.getName() + "_"
                            : prop.getName()
                    )
                    .append(";\n");
        }

        out.closeAndSinkTo(this.targetDir);
    }

}
