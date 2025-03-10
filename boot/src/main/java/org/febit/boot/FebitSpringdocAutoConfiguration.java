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
import io.swagger.v3.core.util.Json;
import org.febit.boot.springdoc.GenericTypeNameResolver;
import org.febit.boot.util.FebitBootBeanNameGenerator;
import org.febit.boot.util.Priority;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({
        SpringDocConfiguration.class
})
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnWebApplication
@ComponentScan(
        basePackageClasses = {
                GenericTypeNameResolver.class
        },
        nameGenerator = FebitBootBeanNameGenerator.class
)
public class FebitSpringdocAutoConfiguration {

    @Bean
    @Order(Priority.HIGHEST)
    public ModelResolver springdocModelResolver(
            Jackson2ObjectMapperBuilder builder
    ) {
        var mapper = Json.mapper();
        builder.configure(mapper);
        return new ModelResolver(mapper, GenericTypeNameResolver.INSTANCE);
    }
}

