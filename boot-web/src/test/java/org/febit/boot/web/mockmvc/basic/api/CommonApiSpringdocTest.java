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
package org.febit.boot.web.mockmvc.basic.api;

import org.febit.boot.web.mockmvc.basic.BaseMvcTest;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.febit.boot.web.mockmvc.JsonPathUtils.PATH_200_JSON_SCHEMA_REF;
import static org.febit.boot.web.mockmvc.JsonPathUtils.j;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommonApiSpringdocTest extends BaseMvcTest {

    @Autowired
    MockMvc mockMvc;

    ResultActions result() throws Exception {
        return mockMvc.perform(get(Constants.DEFAULT_API_DOCS_URL))
                .andExpect(status().isOk());
    }

    @Test
    void spec() throws Exception {
        var result = result()
                .andDo(print());

        // Not Response
        result.andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());

        // OpenAPI Spec
        result.andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").exists())
                .andExpect(jsonPath("$.servers[0].url").exists())
                .andExpect(jsonPath("$.paths").exists())
                .andExpect(jsonPath("$.components.schemas").exists());
    }

    @Test
    void pingApi() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/common/ping'].get";

        result.andExpect(j(prefix + ".tags[0]", "Common API"))
                .andExpect(j(prefix + ".operationId", "ping"))
                .andExpect(j(prefix + "['x-method-name']", "ping"))
                .andExpect(j(
                        prefix + PATH_200_JSON_SCHEMA_REF,
                        "#/components/schemas/IResponse<String>"
                ));
    }

    @Test
    void basicSchema() throws Exception {
        var result = result();
        var prefix = "$.components.schemas['IResponse<String>']";

        result.andExpect(j(prefix + ".type", "object"));

        var props = prefix + ".properties";
        result.andExpect(j(props + ".success.type", "boolean"))
                .andExpect(j(props + ".success['x-class']", "boolean"))
                .andExpect(jsonPath(props + ".status").doesNotExist())
                .andExpect(j(props + ".code.type", "string"))
                .andExpect(j(props + ".code['x-class']", "String"))
                .andExpect(j(props + ".timestamp.type", "string"))
                .andExpect(j(props + ".timestamp['x-class']", "Instant"))
                .andExpect(j(props + ".data.type", "string"))
                .andExpect(j(props + ".data['x-class']", "String"))
        ;
    }
}
