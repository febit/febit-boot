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

import lombok.experimental.UtilityClass;
import org.gradle.api.artifacts.Configuration;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@UtilityClass
public class Utils {

    public static URLClassLoader toClassLoader(Configuration conf) {
        var urls = toUrls(conf);
        return new URLClassLoader(urls);
    }

    public static URL[] toUrls(Configuration conf) {
        return conf.resolve().stream()
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toArray(URL[]::new);
    }
}
