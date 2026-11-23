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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.core.jackson.ModelResolver;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.JacksonMixin;
import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonReadFeature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FebitSpringdocAutoConfigurationTest.App.class)
@AutoConfigureMockMvc
class FebitSpringdocAutoConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelResolver springdocModelResolver;

    @Test
    void swaggerConfigurationProducesUsableSpec() throws Exception {
        openApi()
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.paths['/probe/boot']").exists())
                .andExpect(jsonPath("$.paths['/probe/user']").exists())
                .andExpect(jsonPath("$.components.schemas['BootMixinProbe'].properties.visible.type").value("string"));
    }

    @Test
    void jacksonAutoConfigurationMixinHidesFieldInSpec() throws Exception {
        var props = "$.components.schemas['BootMixinProbe'].properties";
        openApi()
                .andExpect(jsonPath(props + ".visible").exists())
                .andExpect(jsonPath(props + ".hidden").doesNotExist());
    }

    @Test
    void userCustomizerMixinHidesFieldInSpec() throws Exception {
        var props = "$.components.schemas['UserMixinProbe'].properties";
        openApi()
                .andExpect(jsonPath(props + ".visible").exists())
                .andExpect(jsonPath(props + ".hidden").doesNotExist());
    }

    @Test
    void appliesJsonFactoryBuilderCustomizers() {
        var factory = (JsonFactory) springdocModelResolver.objectMapper().tokenStreamFactory();
        assertThat(factory.isEnabled(JsonReadFeature.ALLOW_JAVA_COMMENTS)).isTrue();
    }

    private ResultActions openApi() throws Exception {
        return mockMvc.perform(get(Constants.DEFAULT_API_DOCS_URL))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ImportAutoConfiguration(FebitSpringdocAutoConfiguration.class)
    static class App {

        @Bean
        ProbeController probeController() {
            return new ProbeController();
        }

        @Bean
        JsonMapperBuilderCustomizer userJsonMapperBuilderCustomizer() {
            return builder -> builder.addMixIn(UserMixinProbe.class, UserMixin.class);
        }

        @Bean
        JsonFactoryBuilderCustomizer jsonFactoryBuilderCustomizer() {
            return builder -> builder.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS);
        }
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe/boot")
        BootMixinProbe boot() {
            return null;
        }

        @GetMapping("/probe/user")
        UserMixinProbe user() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    static class BootMixinProbe {

        public String getVisible() {
            return "visible";
        }

        public String getHidden() {
            return "hidden";
        }
    }

    @JacksonMixin(BootMixinProbe.class)
    interface BootMixin {

        @JsonIgnore
        String getHidden();
    }

    @SuppressWarnings("unused")
    static class UserMixinProbe {

        public String getVisible() {
            return "visible";
        }

        public String getHidden() {
            return "hidden";
        }
    }

    interface UserMixin {

        @JsonIgnore
        String getHidden();
    }
}
