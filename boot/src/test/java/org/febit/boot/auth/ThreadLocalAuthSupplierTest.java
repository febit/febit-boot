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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ThreadLocalAuthSupplierTest {

    private ThreadLocalAuthSupplier<AuthSubject> supplier;

    @BeforeEach
    void setUp() {
        supplier = new ThreadLocalAuthSupplier<>();
    }

    @Test
    void emptyInitially() {
        assertThat(supplier.get()).isEmpty();
    }

    @Test
    void setThenGet() {
        var subject = mock(AuthSubject.class);
        supplier.set(subject);
        assertThat(supplier.get()).contains(subject);
    }

    @Test
    void clearRemoves() {
        var subject = mock(AuthSubject.class);
        supplier.set(subject);
        supplier.clear();
        assertThat(supplier.get()).isEmpty();
    }

    @Test
    void supplierScopeRestoresToOuter() {
        var outer = mock(AuthSubject.class);
        var inner = mock(AuthSubject.class);
        supplier.set(outer);

        var result = supplier.scoped(inner, () -> {
            assertThat(supplier.get()).contains(inner);
            return "r";
        });

        assertThat(result).isEqualTo("r");
        assertThat(supplier.get()).contains(outer);
    }

    @Test
    void supplierScopeRestoresToEmptyWhenNoOuter() {
        var inner = mock(AuthSubject.class);

        var result = supplier.scoped(inner, () -> {
            assertThat(supplier.get()).contains(inner);
            return "r";
        });

        assertThat(result).isEqualTo("r");
        assertThat(supplier.get()).isEmpty();
    }

    @Test
    void runnableScopeRestoresToOuter() {
        var outer = mock(AuthSubject.class);
        var inner = mock(AuthSubject.class);
        supplier.set(outer);

        supplier.scoped(inner, () -> assertThat(supplier.get()).contains(inner));

        assertThat(supplier.get()).contains(outer);
    }

    @Test
    void runnableScopeRestoresToEmptyWhenNoOuter() {
        var inner = mock(AuthSubject.class);

        supplier.scoped(inner, () -> assertThat(supplier.get()).contains(inner));

        assertThat(supplier.get()).isEmpty();
    }

    @Test
    void supplierScopePropagatesExceptionAndRestores() {
        var outer = mock(AuthSubject.class);
        var inner = mock(AuthSubject.class);
        supplier.set(outer);

        var ex = assertThrows(RuntimeException.class, () -> supplier.scoped(inner, () -> {
            assertThat(supplier.get()).contains(inner);
            throw new RuntimeException("boom");
        }));
        assertThat(ex.getMessage()).isEqualTo("boom");

        assertThat(supplier.get()).contains(outer);
    }

    @Test
    void runnableScopePropagatesExceptionAndRestores() {
        var outer = mock(AuthSubject.class);
        var inner = mock(AuthSubject.class);
        supplier.set(outer);

        var ex = assertThrows(RuntimeException.class, () -> supplier.scoped(inner, () -> {
            assertThat(supplier.get()).contains(inner);
            throw new RuntimeException("boom");
        }));
        assertThat(ex.getMessage()).isEqualTo("boom");

        assertThat(supplier.get()).contains(outer);
    }
}
