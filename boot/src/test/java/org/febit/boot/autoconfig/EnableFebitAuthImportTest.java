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

import org.febit.boot.EnableFebitAuth;
import org.febit.boot.auth.component.WebAuthHandlerImpl;
import org.febit.boot.permission.AnnotatedMethodPermissionResolver;
import org.febit.boot.web.mockmvc.auth.component.AuthImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EnableFebitAuthImportTest.App.class)
class EnableFebitAuthImportTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void importsFebitAuthConfiguration() {
        assertThat(context.getBean(AnnotatedMethodPermissionResolver.class)).isNotNull();
        assertThat(context.getBean(WebAuthHandlerImpl.class)).isNotNull();
    }

    @EnableFebitAuth
    @SpringBootConfiguration
    @Import(AuthImpl.class)
    static class App {
    }
}
