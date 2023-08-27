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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.febit.boot.web.mockmvc.JsonPathUtils.j;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommonApiTest extends BaseMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void ping() throws Exception {
        mockMvc.perform(get("/api/v1/common/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(j("$.success", true))
                .andExpect(j("$.data", "pong"))
        ;
    }

    @Test
    void error400() throws Exception {
        mockMvc.perform(get("/api/v1/common/error-400"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(j("$.code", "BAD_REQUEST"))
                .andExpect(j("$.message", "Bad request 400"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(j("$.success", false))
                .andExpect(jsonPath("$.data").doesNotExist())
        ;
    }

    @Test
    void exception403() throws Exception {
        mockMvc.perform(get("/api/v1/common/exception-403"))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(j("$.code", "FORBIDDEN"))
                .andExpect(j("$.message", "Exception FORBIDDEN"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(j("$.success", false))
                .andExpect(jsonPath("$.data").doesNotExist())
        ;
    }

}
