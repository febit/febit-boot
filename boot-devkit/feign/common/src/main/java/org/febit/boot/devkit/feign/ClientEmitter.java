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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.devkit.feign.meta.ApiDef;
import org.febit.boot.devkit.feign.meta.ApiParameterDef;
import org.febit.boot.devkit.feign.meta.ClientDef;
import org.febit.boot.devkit.feign.util.ClassWriter;
import org.febit.boot.devkit.feign.util.ImportSet;
import org.febit.devkit.gradle.util.JavaUtils;

import java.util.List;

@Slf4j
class ClientEmitter {

    private static final List<String> DEFAULT_IMPORTS = List.of(
            "org.febit.lang.protocol.*",
            "org.springframework.cloud.openfeign.SpringQueryMap",
            "org.springframework.web.bind.annotation.*"
    );

    private final ClassWriter out;
    private final ImportSet imports;

    private final ClientCodegen codegen;
    private final ClientDef client;

    private final String clientPath;
    private final String apiPathPrefix;

    ClientEmitter(ClientCodegen codegen, ClientDef client) {
        this.codegen = codegen;
        this.client = client;

        var fullName = codegen.clientNaming.resolve(client.getType().getCanonicalName());
        this.out = ClassWriter.create(codegen.pojoNaming, fullName);
        this.imports = this.out.getImports();

        DEFAULT_IMPORTS.forEach(this.imports::add);

        var path = client.getRequest().getPrimaryPath();
        // NOTE: Path-Variables are not support in client-level settings.
        this.clientPath = StringUtils.substringBefore(path, "/{");
        this.apiPathPrefix = StringUtils.removeEnd(
                path.substring(this.clientPath.length()),
                "/"
        );
    }

    void emit() {
        log.info("Emit client: {}", this.out.getFullName());

        // Imports
        client.getApis().forEach(api -> {
            imports.add(api.getReturnType());
            api.getParameters()
                    .stream()
                    .map(ApiParameterDef::getType)
                    .forEach(imports::add);
        });
        codegen.clientConfigClasses.forEach(imports::add);

        // Emit POJOs
        client.getApis().forEach(api -> {
            codegen.emitPojoIfAbsent(api.getReturnType());
            api.getParameters()
                    .stream()
                    .map(ApiParameterDef::getType)
                    .forEach(codegen::emitPojoIfAbsent);
        });

        out.appendClassHeader();

        if (client.isDeprecated()) {
            out.append("@Deprecated\n");
        }

        emitClientConfigs();
        out.appendDeclare("interface");

        client.getApis()
                .forEach(this::emitApi);

        out.closeAndSinkTo(codegen.targetDir);
    }

    private void emitClientConfigs() {

        out.append("@org.springframework.cloud.openfeign.FeignClient(\n")
                .tab(2).append("configuration = {\n");

        out.foreach(codegen.clientConfigClasses, ",\n", "\n", cls ->
            out.tab(4).appendClass(cls)
                    .append(".class")
        );

        out.tab(2).append("},\n")
                .tab(2).append("name = ").appendDirectValue(codegen.clientName).append(",\n")
                .tab(2).append("url = ").appendDirectValue(codegen.clientUrl).append(",\n")
                .tab(2).append("contextId = ").appendDirectValue(this.out.getFullName()).append(",\n")
                .tab(2).append("path = ").appendDirectValue(clientPath)
                .append("\n)\n");
    }

    private void emitApi(ApiDef api) {
        out.append('\n');

        if (api.isDeprecated()) {
            out.tab(1).append("@Deprecated\n");
        }

        emitApiRequestMapping(api);

        out.tab(1).append("")
                .appendType(api.getReturnType())
                .append(" ")
                .append(api.getName())
                .append('(');

        var params = api.getParameters();
        if (!params.isEmpty()) {
            out.append('\n');
            out.foreach(params, ",\n", "\n", this::emitApiParameter);
            out.tab(1);
        }

        out.append(");\n");
    }

    private void emitApiRequestMapping(ApiDef api) {
        var req = api.getRequest();

        out.tab(1).append("");
        if (req.getMethods().size() != 1) {
            out.append("@RequestMapping(\n");
            out.tab(2).append("method = {\n");
            out.foreach(req.getMethods(), ",\n", "\n", method ->
                    out.tab(3).append("RequestMethod.").append(method.name())
            );
            out.tab(2).append("},\n");
        } else {
            out.append("@")
                    .append(JavaUtils.upperFirst(req.getMethods().get(0).name().toLowerCase()))
                    .append("Mapping(\n");
        }

        String path = req.getPrimaryPath();
        if (!path.isEmpty()) {
            path = StringUtils.prependIfMissing(path, "/");
        }

        out.tab(2).append("path = ")
                .appendDirectValue(this.apiPathPrefix + path);

        if (!req.getHeaders().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("headers = {\n");
            out.foreach(req.getHeaders(), ",\n", "\n", str ->
                    out.tab(3).appendDirectValue(str)
            );
            out.tab(2).append("}");
        }

        if (!req.getConsumes().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("consumes = {\n");
            out.foreach(req.getConsumes(), ",\n", "\n", str ->
                    out.tab(3).appendDirectValue(str)
            );
            out.tab(2).append("}");
        }

        if (!req.getProduces().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("produces = {\n");
            out.foreach(req.getProduces(), ",\n", "\n", str ->
                    out.tab(3).appendDirectValue(str)
            );
            out.tab(2).append("}");
        }

        out.append('\n');

        // Close annotation
        out.tab(1).append(")\n");
    }

    private void emitApiParameter(ApiParameterDef param) {
        out.tab(3);

        var bindName = StringUtils.isNotEmpty(param.getBindName())
                ? param.getBindName()
                : param.getName();

        if (param.getIsDeprecated()) {
            out.append("@Deprecated ");
        }

        switch (param.getIn()) {
            case QUERY:
                out.append("@RequestParam(")
                        .appendDirectValue(bindName)
                        .append(") ");
                break;
            case QUERY_MAP:
                out.append("@SpringQueryMap ");
                break;
            case PATH_VAR:
                out.append("@PathVariable(")
                        .appendDirectValue(bindName)
                        .append(") ");
                break;
            case HEADER:
                out.append("@RequestHeader(")
                        .appendDirectValue(bindName)
                        .append(") ");
                break;
            case MODEL_ATTR:
                out.append("@ModelAttribute ");
                break;
            case BODY:
                out.append("@RequestBody ");
                break;
            default:
                throw new IllegalArgumentException("Unsupported Parameter.In: " + param.getIn());
        }
        out.appendType(param.getType())
                .append(' ')
                .append(param.getName());
    }
}
