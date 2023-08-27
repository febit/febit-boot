package org.febit.boot.common.auth;

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
