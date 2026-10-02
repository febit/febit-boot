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
package org.febit.boot.autoconfig;

import org.febit.boot.FebitWebAutoConfiguration;
import org.febit.boot.web.component.mvc.WebCommonMvcConfigurer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = FebitWebAutoConfigurationConditionTest.App.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class FebitWebAutoConfigurationConditionTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void webComponentsAbsentInNonWebApplication() {
        assertThat(context.getBeansOfType(WebCommonMvcConfigurer.class)).isEmpty();
    }

    @SpringBootConfiguration
    @Import(FebitWebAutoConfiguration.class)
    static class App {
    }
}
