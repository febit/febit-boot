package org.febit.boot.web.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.febit.boot.common.auth.AuthConstants;
import org.febit.boot.common.auth.AuthSubject;
import org.febit.boot.common.auth.AuthSupplier;
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
