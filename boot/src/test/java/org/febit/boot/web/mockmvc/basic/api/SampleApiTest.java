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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.febit.boot.web.mockmvc.JsonPathUtils.j;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SampleApiTest extends BaseMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void requireById() throws Exception {
        mockMvc.perform(get("/api/v1/samples/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(j("$.data.id", 1))
        ;
    }

    @Test
    void search() throws Exception {
        mockMvc.perform(post("/api/v1/samples/search?page=2&size=20")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.rows[0]").exists())
                .andExpect(j("$.data.meta.page", 2))
                .andExpect(j("$.data.meta.size", 20))
                .andExpect(j("$.data.rows[0].id", 1))
        ;
    }

}
