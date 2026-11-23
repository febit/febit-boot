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
package org.febit.boot;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.MapperCustomizer;
import io.swagger.v3.core.util.ObjectMapperFactory;
import org.febit.boot.springdoc.ExtraMetaOperationCustomizer;
import org.febit.boot.springdoc.swagger.GenericTypeNameResolver;
import org.febit.boot.util.FebitBootBeanNameGenerator;
import org.febit.boot.util.Priority;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import tools.jackson.core.json.JsonFactoryBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import lombok.extern.slf4j.Slf4j;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({
        SpringDocConfiguration.class
})
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnWebApplication
@ComponentScan(
        basePackageClasses = {
                ExtraMetaOperationCustomizer.class
        },
        nameGenerator = FebitBootBeanNameGenerator.class
)
public class FebitSpringdocAutoConfiguration {

    private static MapperCustomizer customizer(ObjectProvider<JsonMapperBuilderCustomizer> customizers) {
        return (b, target) -> {
            if (!(b instanceof JsonMapper.Builder builder)) {
                log.debug("Skip customize ObjectMapper used by springdoc, because it's not JsonMapper.Builder, but {}", b.getClass());
                return;
            }
            customizers.forEach(
                    customizer -> customizer.customize(builder)
            );
        };
    }

    @Bean
    @Order(Priority.HIGHEST)
    public ModelResolver springdocModelResolver(
            ObjectProvider<JsonMapperBuilderCustomizer> customizers,
            ObjectProvider<JsonFactoryBuilderCustomizer> jsonFactoryBuilderCustomizers
    ) {
        var jsonFactoryBuilder = new JsonFactoryBuilder();
        jsonFactoryBuilderCustomizers.forEach(customizer -> customizer.customize(jsonFactoryBuilder));

        var customizer = customizer(customizers);
        ObjectMapper mapper;
        try {
            ObjectMapperFactory.addCustomizer(customizer);
            mapper = ObjectMapperFactory.createJson31(jsonFactoryBuilder.build());
        } finally {
            ObjectMapperFactory.removeCustomizer(customizer);
        }
        return new ModelResolver(mapper, GenericTypeNameResolver.INSTANCE);
    }
}
