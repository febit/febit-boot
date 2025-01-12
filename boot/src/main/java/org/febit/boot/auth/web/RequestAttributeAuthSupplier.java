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
package org.febit.boot.auth.web;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.febit.boot.auth.AuthConstants;
import org.febit.boot.auth.AuthSubject;
import org.febit.boot.auth.AuthSupplier;
import org.febit.boot.common.util.Errors;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestAttributeAuthSupplier<T extends AuthSubject> implements AuthSupplier<T> {

    private final String key;
    private final Class<T> authType;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> get() {
        val attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return Optional.empty();
        }
        var raw = attrs.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
        if (raw == null) {
            return Optional.empty();
        }
        if (!authType.isInstance(raw)) {
            throw Errors.SYSTEM.exception("Auth type not matched.");
        }
        return Optional.of((T) raw);
    }

    public static <T extends AuthSubject> RequestAttributeAuthSupplier<T> create(
            String key, Class<T> type
    ) {
        return new RequestAttributeAuthSupplier<>(key, type);
    }

    public static <T extends AuthSubject> RequestAttributeAuthSupplier<T> create(
            Class<T> type
    ) {
        return create(AuthConstants.ATTR_AUTH, type);
    }

}
