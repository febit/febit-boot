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
package org.febit.boot.devkit.jooq.gradle;

import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.boot.devkit.flyway.gradle.model.JdbcOptionImpl;
import org.gradle.api.provider.Provider;
import org.jooq.meta.jaxb.Jdbc;

public record PresetJdbcProvider(
        Provider<Jdbc> jdbc
) implements JdbcProvider<PresetJdbcProvider.Params> {

    public interface Params {
    }

    @Override
    public JdbcOption prepare(Params params) {
        var resolved = jdbc.get();
        return JdbcOptionImpl.builder()
                .url(resolved.getUrl())
                .user(resolved.getUser())
                .password(resolved.getPassword())
                .build();
    }

    @Override
    public void close() {
    }
}
