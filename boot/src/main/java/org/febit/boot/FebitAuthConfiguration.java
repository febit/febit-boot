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

import org.febit.boot.auth.component.WebAuthHandlerImpl;
import org.febit.boot.permission.AnnotatedMethodPermissionResolver;
import org.febit.boot.util.FebitBootBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
        nameGenerator = FebitBootBeanNameGenerator.class,
        basePackageClasses = {
                WebAuthHandlerImpl.class
        }
)
public class FebitAuthConfiguration {

    @Bean
    public AnnotatedMethodPermissionResolver annotatedMethodPermissionResolver() {
        return new AnnotatedMethodPermissionResolver();
    }
}
