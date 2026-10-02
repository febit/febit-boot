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
package org.febit.boot.model;

import org.febit.boot.auth.AuthSubject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IAuditableTest {

    @Test
    void createdFillsAllFieldsFromSubject() {
        var target = new AuditableFixture();
        var subject = new Subject("alice");

        target.created(subject);

        assertThat(target.createdBy).isEqualTo("alice");
        assertThat(target.updatedBy).isEqualTo("alice");
        assertThat(target.createdAt).isNotNull();
        assertThat(target.updatedAt).isEqualTo(target.createdAt);
    }

    @Test
    void updatedFillsOnlyUpdateFields() {
        var target = new AuditableFixture();
        var subject = new Subject("bob");

        target.updated(subject);

        assertThat(target.updatedBy).isEqualTo("bob");
        assertThat(target.updatedAt).isNotNull();
        assertThat(target.createdBy).isNull();
        assertThat(target.createdAt).isNull();
    }

    record Subject(String id) implements AuthSubject {
        @Override
        public String identifier() {
            return id;
        }
    }

    static class AuditableFixture implements IAuditable {
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;

        @Override
        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }

        @Override
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        @Override
        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }
    }
}
