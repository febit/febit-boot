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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.common.permission.AnonymousApi;
import org.febit.boot.web.mockmvc.auth.Permissions;
import org.febit.lang.protocol.IBasicApi;
import org.febit.lang.protocol.IResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Auth API")
@RequestMapping(value = {
        "/api/v1/auth"
}, produces = {
        MediaType.APPLICATION_JSON_VALUE
})
public class AuthApi implements IBasicApi {

    @GetMapping(value = "/missing-permission")
    public IResponse<Void> missingPermission() {
        return ok();
    }

    @AnonymousApi
    @GetMapping(value = "/login")
    public IResponse<Void> login() {
        return ok();
    }

    @Permissions.Foo
    @AnonymousApi
    @GetMapping(value = "/ping")
    public IResponse<Void> ping() {
        return ok();
    }

    @AnonymousApi(false)
    @Permissions.Foo
    @GetMapping(value = "/not-anonymous")
    public IResponse<Void> notAnonymous() {
        return ok();
    }

    @Permissions.Foo
    @GetMapping(value = "/foo")
    public IResponse<Void> foo() {
        return ok();
    }

    @Permissions.Bar
    @GetMapping(value = "/bar")
    public IResponse<Void> bar() {
        return ok();
    }

    @Permissions.Baz("qux")
    @Permissions.Foo
    @Permissions.Bar
    @GetMapping(value = "/qux")
    public IResponse<Void> qux() {
        return ok();
    }
}
