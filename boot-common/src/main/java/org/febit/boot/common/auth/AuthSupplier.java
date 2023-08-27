package org.febit.boot.common.auth;

import java.util.Optional;

@FunctionalInterface
public interface AuthSupplier<T extends AuthSubject> {

    Optional<T> get();

    default String getSupplierName() {
        return getClass().getSimpleName();
    }
}
