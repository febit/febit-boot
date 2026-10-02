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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSuppliersTest {

    private static final AuthSubject SUBJECT_A = new TestSubject("a");
    private static final AuthSubject SUBJECT_B = new TestSubject("b");

    @Test
    void returnsNullWhenNoSuppliers() {
        assertThat(AuthSuppliers.get(List.<AuthSupplier<AuthSubject>>of())).isNull();
    }

    @Test
    void returnsNullWhenAllEmpty() {
        var empty1 = emptySupplier();
        var empty2 = emptySupplier();

        assertThat(AuthSuppliers.get(List.of(empty1, empty2))).isNull();
        assertThat(empty1.queried).isTrue();
        assertThat(empty2.queried).isTrue();
    }

    @Test
    void returnsFirstNonEmptySubject() {
        var empty = emptySupplier();
        var winner = supplierReturning(SUBJECT_A);
        var later = supplierReturning(SUBJECT_B);

        assertThat(AuthSuppliers.get(List.of(empty, winner, later))).isSameAs(SUBJECT_A);
    }

    @Test
    void doesNotQuerySuppliersAfterFirstNonEmpty() {
        var empty = emptySupplier();
        var winner = supplierReturning(SUBJECT_A);
        var later = supplierReturning(SUBJECT_B);

        AuthSuppliers.get(List.of(empty, winner, later));

        assertThat(empty.queried).isTrue();
        assertThat(winner.queried).isTrue();
        assertThat(later.queried).isFalse();
    }

    @Test
    void keepsFirstWhenMultipleNonEmpty() {
        var first = supplierReturning(SUBJECT_A);
        var second = supplierReturning(SUBJECT_B);

        assertThat(AuthSuppliers.get(List.of(first, second))).isSameAs(SUBJECT_A);
        assertThat(second.queried).isFalse();
    }

    private static StubSupplier emptySupplier() {
        return new StubSupplier(Optional.empty());
    }

    private static StubSupplier supplierReturning(AuthSubject subject) {
        return new StubSupplier(Optional.of(subject));
    }

    private static final class StubSupplier implements AuthSupplier<AuthSubject> {

        private final Optional<AuthSubject> value;
        private boolean queried;

        StubSupplier(Optional<AuthSubject> value) {
            this.value = value;
        }

        @Override
        public Optional<AuthSubject> get() {
            queried = true;
            return value;
        }
    }

    private record TestSubject(String id) implements AuthSubject {
        @Override
        public String identifier() {
            return id;
        }
    }
}
