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

import lombok.Data;

import java.io.File;

@Data
public class EmbeddedPostgresConfig {

    public static final String DEFAULT_VERSION = "14.8.0";

    /**
     * Version of embedded-postgres-binaries-bom.
     * Ref: <a href="https://central.sonatype.com/artifact/io.zonky.test.postgres/embedded-postgres-binaries-bom/">...</a>
     */
    private String version = DEFAULT_VERSION;
    private File workingDir;
}
