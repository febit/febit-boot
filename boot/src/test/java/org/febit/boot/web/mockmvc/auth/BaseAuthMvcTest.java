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
package org.febit.boot.web.mockmvc.auth;

import org.febit.boot.FebitAuthConfiguration;
import org.febit.boot.FebitSpringdocAutoConfiguration;
import org.febit.boot.FebitWebAutoConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = {BaseAuthMvcTest.TestApplication.class})
@ActiveProfiles("unit-test")
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public abstract class BaseAuthMvcTest {

    @SpringBootApplication
    @ImportAutoConfiguration(
            classes = {
                    FebitSpringdocAutoConfiguration.class,
                    FebitWebAutoConfiguration.class,
                    FebitAuthConfiguration.class,
            }
    )
    public static class TestApplication {

        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }
    }
}
