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
import org.springdoc.core.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.febit.boot.web.mockmvc.JsonPathUtils.j;
import static org.febit.boot.web.mockmvc.JsonPathUtils.jsonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiSpringdocTest extends BaseAuthMvcTest {

    @Autowired
    MockMvc mockMvc;

    ResultActions result() throws Exception {
        return mockMvc.perform(get(Constants.DEFAULT_API_DOCS_URL))
                .andExpect(status().isOk());
    }

    @Test
    void qux() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/qux'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "ALLOW_LIST"))
                .andExpect(jsonList(prefix + ".items", List.of(
                        "api.baz:qux",
                        "api.foo",
                        "api:bar"
                )));
    }

    @Test
    void foo() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/foo'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "ALLOW_LIST"))
                .andExpect(jsonList(prefix + ".items", List.of(
                        "api.foo"
                )));
    }

    @Test
    void bar() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/bar'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "ALLOW_LIST"))
                .andExpect(jsonList(prefix + ".items", List.of(
                        "api:bar"
                )));
    }

    @Test
    void notAnonymous() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/not-anonymous'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "ALLOW_LIST"))
                .andExpect(jsonList(prefix + ".items", List.of(
                        "api.foo"
                )));
    }

    @Test
    void ping() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/ping'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "IGNORED"))
                .andExpect(jsonList(prefix + ".items", List.of()));
    }

    @Test
    void login() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/login'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "IGNORED"))
                .andExpect(jsonList(prefix + ".items", List.of()));
    }

    @Test
    void missingPermission() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/auth/missing-permission'].get['x-permission']";

        result.andExpect(j(prefix + ".type", "FORBIDDEN"))
                .andExpect(jsonList(prefix + ".items", List.of()));
    }
}
