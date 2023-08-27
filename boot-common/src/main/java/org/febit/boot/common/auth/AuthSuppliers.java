package org.febit.boot.common.auth;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@UtilityClass
public class AuthSuppliers {

    @Nullable
    public static <T extends AuthSubject, S extends AuthSupplier<? extends T>> T get(
            Collection<S> suppliers
    ) {
        for (var supplier : suppliers) {
            var auth = supplier.get();
            if (auth.isEmpty()) {
                continue;
            }
            log.debug("Got auth subject supplied by [{}]", supplier.getSupplierName());
            return auth.get();
        }
        return null;
    }
}
