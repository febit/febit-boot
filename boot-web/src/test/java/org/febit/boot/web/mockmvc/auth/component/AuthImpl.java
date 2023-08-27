package org.febit.boot.web.mockmvc.auth.component;

import org.febit.boot.common.permission.PermissionItem;
import org.febit.boot.common.permission.PermissionVerifier;
import org.febit.boot.web.auth.WebRequestAuthSubjectResolver;
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
        if (ADMIN.equals(auth.getCode())) {
            return true;
        }
        var permissions = permissionsMap.getOrDefault(auth.getCode(), Set.of());
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
        var auth = new TestAuthSubject();
        auth.setCode(code);
        return auth;
    }
}
