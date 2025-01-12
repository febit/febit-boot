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

import java.util.Optional;
import java.util.function.Supplier;

public class ThreadLocalAuthSupplier<T extends AuthSubject> implements AuthSupplier<T> {

    private final ThreadLocal<T> holder = new ThreadLocal<>();

    public void set(@Nullable T auth) {
        holder.set(auth);
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(holder.get());
    }

    public void clear() {
        holder.remove();
    }

    public <V> V scoped(T auth, Supplier<V> supplier) {
        var original = holder.get();
        set(auth);
        try {
            return supplier.get();
        } finally {
            set(original);
        }
    }

    public void scoped(T auth, Runnable runnable) {
        var original = holder.get();
        set(auth);
        try {
            runnable.run();
        } finally {
            set(original);
        }
    }
}
