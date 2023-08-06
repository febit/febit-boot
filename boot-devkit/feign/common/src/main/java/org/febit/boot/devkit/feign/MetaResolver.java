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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.febit.boot.devkit.feign.meta.ApiDef;
import org.febit.boot.devkit.feign.meta.ApiParameterDef;
import org.febit.boot.devkit.feign.meta.ClientDef;
import org.febit.boot.devkit.feign.meta.RequestDef;
import org.febit.boot.devkit.feign.util.CodeUtils;
import org.febit.lang.UncheckedException;
import org.febit.lang.protocal.IListResponse;
import org.febit.lang.protocal.IPageResponse;
import org.febit.lang.protocal.IResponse;
import org.febit.lang.protocal.Page;
import org.febit.lang.util.Maps;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(staticName = "create")
public class MetaResolver {

    private final ClassLoader classloader;

    private final Set<String> excludeParamTypes = Set.of(
            "javax.servlet.http.HttpServletRequest",
            "javax.servlet.http.HttpServletResponse",
            "javax.servlet.http.HttpSession"
    );

    public Map<String, BeanDefinition> scanBeans(Collection<String> packages) {
        val registry = new SimpleBeanDefinitionRegistry();

        val scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(new DefaultResourceLoader(classloader));
        scanner.addIncludeFilter((r, f) -> true);
        scanner.setBeanNameGenerator((def, reg) -> Objects.requireNonNull(
                def.getBeanClassName()
        ));
        scanner.scan(packages.toArray(new String[0]));

        return Maps.compute(
                registry.getBeanDefinitionNames(),
                registry::getBeanDefinition
        );
    }

    public List<ClientDef> resolveClients(Collection<BeanDefinition> beans) {
        return beans.stream()
                .filter(def -> def instanceof AnnotatedBeanDefinition)
                .map(def -> (AnnotatedBeanDefinition) def)
                .filter(this::isController)
                .map(this::resolveClient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private ClientDef resolveClient(AnnotatedBeanDefinition def) {
        Class<?> type;
        try {
            type = classloader.loadClass(def.getBeanClassName());
        } catch (ClassNotFoundException e) {
            throw new UncheckedException(e);
        }
        val mappingAnno = AnnotatedElementUtils.findMergedAnnotation(
                type, RequestMapping.class);
        if (mappingAnno == null) {
            // GradleUtils.println("Not found RequestMapping for: {0}", type);
            return null;
        }

        val builder = ClientDef.builder()
                .type(type)
                .deprecated(CodeUtils.isDeprecated(type))
                .request(RequestDef.of(mappingAnno));

        ReflectionUtils.doWithMethods(type,
                m -> {
                    val api = resolveApi(m, type);
                    if (api != null) {
                        builder.api(api);
                    }
                },
                m -> !Modifier.isStatic(m.getModifiers()));

        return builder.build();
    }

    @Nullable
    private ApiDef resolveApi(Method method, Class<?> parentClass) {
        val mappingAnno = AnnotatedElementUtils.findMergedAnnotation(
                method, RequestMapping.class);
        if (mappingAnno == null) {
            return null;
        }

        return ApiDef.builder()
                .name(method.getName())
                .deprecated(CodeUtils.isDeprecated(method))
                .returnType(fixReturnType(
                        ResolvableType.forMethodReturnType(method, parentClass)
                ))
                .request(RequestDef.of(mappingAnno))
                .parameters(resolveParameters(method))
                .build();
    }

    private ResolvableType replaceResponseImpl(ResolvableType type) {
        val params = type.getGenerics();
        if (params.length != 1) {
            return type;
        }
        val dataType = params[0];
        val dataCls = dataType.resolve(Object.class);
        if (List.class.equals(dataCls)
                || Collection.class.equals(dataCls)) {
            return ResolvableType.forClassWithGenerics(IListResponse.class, dataType.getGenerics());
        }
        if (Page.class.equals(dataCls)) {
            return ResolvableType.forClassWithGenerics(IPageResponse.class, dataType.getGenerics());
        }
        return type;
    }

    private ResolvableType fixReturnType(ResolvableType origin) {
        val rawCls = origin.resolve(Object.class);
        if (IResponse.class.equals(rawCls)) {
            return replaceResponseImpl(origin);
        }
        return origin;
    }

    private List<ApiParameterDef> resolveParameters(Method method) {
        return Stream.of(method.getParameters())
                .map(ApiParameterDef::of)
                .filter(this::isParameterInclude)
                .collect(Collectors.toList());
    }

    private boolean isParameterInclude(ApiParameterDef parameter) {
        return !excludeParamTypes.contains(
                parameter.getType().resolve(Object.class).getName()
        );
    }

    private boolean isController(AnnotatedBeanDefinition def) {
        return def.getMetadata()
                .hasAnnotation("org.springframework.web.bind.annotation.RestController");
    }

}
