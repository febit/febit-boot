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
package org.febit.boot.web.mockmvc.auth.component;

import org.febit.boot.auth.web.WebRequestAuthSubjectResolver;
import org.febit.boot.common.permission.PermissionItem;
import org.febit.boot.common.permission.PermissionVerifier;
import org.febit.lang.annotation.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@NonNullApi
@Component
public class AuthImpl implements PermissionVerifier<TestAuthSubject>, WebRequestAuthSubjectResolver<TestAuthSubject> {

    public static final String HEADER_AUTH = "X-Auth-Code";
    public static final String ADMIN = "admin";

    private static final Map<String, Set<String>> permissionsMap = Map.of(
            "foo", Set.of("api.foo"),
            "bar", Set.of("api:bar"),
            "foobar", Set.of(
                    "api:bar",
                    "api.foo"
            )
    );

    @Override
    public boolean isAllow(TestAuthSubject auth, Collection<PermissionItem> allows) {
        if (ADMIN.equals(auth.identifier())) {
            return true;
        }
        var permissions = permissionsMap.getOrDefault(auth.identifier(), Set.of());
        for (var allow : allows) {
            if (permissions.contains(allow.getCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TestAuthSubject> resolveAuth(WebRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_AUTH))
                .map(this::parseAuth);
    }

    private TestAuthSubject parseAuth(String code) {
        return new TestAuthSubject(code, code);
    }
}
