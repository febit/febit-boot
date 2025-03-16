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

import org.febit.boot.devkit.jooq.gradle.container.DbType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.febit.boot.devkit.jooq.gradle.PluginTestScene.SCHEMA_FOO;
import static org.febit.boot.devkit.jooq.gradle.PluginTestScene.TARGET_DIR;
import static org.junit.jupiter.api.Assertions.*;

class PluginContainerMariadbTest {

    @Test
    void tasks() throws IOException {
        var scene = PluginTestScene.create("mysql", "mariadb-container");
        scene.containerJdbc(conf ->
                conf.setType(DbType.MARIADB)
        );

        var forcedTypes = scene.forcedTypes();
        forcedTypes.timeToInstant();
        forcedTypes.mapping(Map.of(
                "foo", SCHEMA_FOO
        ));

        scene.execute();

        assertTrue(scene.isFileExists(TARGET_DIR + "/po/FooPO.java"));
        assertTrue(scene.isFileExists(TARGET_DIR + "/record/FooRecord.java"));
        assertTrue(scene.isFileExists(TARGET_DIR + "/table/TFoo.java"));
    }
}
