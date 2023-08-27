package org.febit.boot.common.auth;

public interface DelegateAuthSubject<T extends AuthSubject> extends AuthSubject {

    T delegated();

    @Override
    default String getCode() {
        return delegated().getCode();
    }
}
