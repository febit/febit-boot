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
package org.febit.boot.auth;

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
