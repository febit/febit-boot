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
package org.febit.boot.devkit.jooq.gradle.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DbType {
    POSTGRES(
            "postgres:16.6",
            "postgres",
            "org.postgresql:postgresql",
            "postgresql",
            "org.postgresql.Driver"
    ),
    MARIADB(
            "mariadb:10.11",
            "mysql",
            "com.mysql:mysql-connector-j",
            "mysql",
            "com.mysql.cj.jdbc.Driver"
    ),
    MYSQL(
            "mysql:8.0",
            "mysql",
            "com.mysql:mysql-connector-j",
            "mysql",
            "com.mysql.cj.jdbc.Driver"
    );

    private final String image;
    private final String profile;
    private final String driverArtifact;
    private final String jdbcPrefix;
    private final String driverClassName;
}
