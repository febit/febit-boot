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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@Slf4j
@Disabled
class ContainerDatabaseIT {

    @Test
    void start() throws IOException {
        var workingDir = new File("build/test-container-db");
        log.info("Container DB Workdir: {}", workingDir.getAbsoluteFile());

        var db = ContainerDatabase.builder()
                // .type(DbType.POSTGRES)
                .type(DbType.MYSQL)
                .workingDir(workingDir)
                .database("test")
                .user("user")
                .password("pass")
                .build();
        db.start();
        log.info("==== Is ready: {}", db.isReady());
        db.close();
    }
}
