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
package org.febit.boot.devkit.feign.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ClassNamings {

    public static ClassNaming cache(ClassNaming naming) {
        var caching = new ConcurrentHashMap<String, String>(128);
        return origin -> caching.computeIfAbsent(origin, naming::resolve);
    }

    public static ClassNaming chain(ClassNaming... namings) {
        return chain(List.of(namings));
    }

    public static ClassNaming chain(Collection<ClassNaming> namings) {
        var copied = List.copyOf(namings);
        return cache(origin -> {
            for (var naming : copied) {
                origin = naming.resolve(origin);
            }
            return origin;
        });
    }

}
