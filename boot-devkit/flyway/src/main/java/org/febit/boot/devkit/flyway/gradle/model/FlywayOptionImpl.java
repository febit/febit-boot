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
package org.febit.boot.devkit.flyway.gradle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder(
        builderClassName = "Builder"
)
public class FlywayOptionImpl implements FlywayOption {

    private String encoding;
    private String table;
    private String tablespace;

    @Singular
    private List<String> schemas;
    @Singular
    private List<String> migrationsDirs;

    @Nullable
    @Override
    public String encoding() {
        return encoding;
    }

    @Nullable
    @Override
    public String table() {
        return table;
    }

    @Nullable
    @Override
    public String tablespace() {
        return tablespace;
    }

    @Nullable
    @Override
    public List<String> schemas() {
        return schemas;
    }

    @Nullable
    @Override
    public List<String> migrationsDirs() {
        return migrationsDirs;
    }
}
