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
package org.febit.boot.web.mockmvc.auth.api;

import org.febit.boot.web.mockmvc.auth.BaseAuthMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.febit.boot.web.mockmvc.auth.component.AuthImpl.HEADER_AUTH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends BaseAuthMvcTest {

    static final String PREFIX = "/api/v1/auth";

    @Autowired
    MockMvc mockMvc;

    @Test
    void ping() throws Exception {
        mockMvc.perform(get(PREFIX + "/ping"))
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/ping")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().isOk())
        ;
    }

    @Test
    void login() throws Exception {
        mockMvc.perform(get(PREFIX + "/login"))
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/login")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().isOk())
        ;
    }

    @Test
    void missingPermission() throws Exception {
        mockMvc.perform(get(PREFIX + "/missing-permission"))
                .andExpect(status().is(403))
        ;
        mockMvc.perform(get(PREFIX + "/missing-permission")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().is(403))
        ;
    }

    @Test
    void notAnonymous() throws Exception {
        mockMvc.perform(get(PREFIX + "/not-anonymous"))
                .andExpect(status().is(401))
        ;
        mockMvc.perform(get(PREFIX + "/not-anonymous")
                        .header(HEADER_AUTH, "foobar")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/not-anonymous")
                        .header(HEADER_AUTH, "foo")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/not-anonymous")
                        .header(HEADER_AUTH, "bar")
                )
                .andExpect(status().is(403))
        ;
        mockMvc.perform(get(PREFIX + "/not-anonymous")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().isOk())
        ;
    }

    @Test
    void foo() throws Exception {
        mockMvc.perform(get(PREFIX + "/foo"))
                .andExpect(status().is(401))
        ;
        mockMvc.perform(get(PREFIX + "/foo")
                        .header(HEADER_AUTH, "foo")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/foo")
                        .header(HEADER_AUTH, "foobar")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/foo")
                        .header(HEADER_AUTH, "bar")
                )
                .andExpect(status().is(403))
        ;
        mockMvc.perform(get(PREFIX + "/foo")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().isOk())
        ;
    }

    @Test
    void bar() throws Exception {
        mockMvc.perform(get(PREFIX + "/bar"))
                .andExpect(status().is(401))
        ;
        mockMvc.perform(get(PREFIX + "/bar")
                        .header(HEADER_AUTH, "foo")
                )
                .andExpect(status().is(403))
        ;
        mockMvc.perform(get(PREFIX + "/bar")
                        .header(HEADER_AUTH, "bar")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/bar")
                        .header(HEADER_AUTH, "foobar")
                )
                .andExpect(status().isOk())
        ;
        mockMvc.perform(get(PREFIX + "/bar")
                        .header(HEADER_AUTH, "admin")
                )
                .andExpect(status().isOk())
        ;
    }

}
