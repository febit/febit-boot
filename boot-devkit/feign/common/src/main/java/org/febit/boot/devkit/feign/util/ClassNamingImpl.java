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

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassNamingImpl implements ClassNaming {

    private final Map<String, String> mapping = new HashMap<>();

    private final List<Tuple2<String, String>> pkgPairs = new ArrayList<>();
    private final List<Tuple2<String, String>> suffixPairs = new ArrayList<>();

    private String prefix;
    private String suffix;

    public ClassNamingImpl prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ClassNamingImpl suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public ClassNamingImpl replace(String from, String to) {
        mapping.put(from, to);
        return this;
    }

    public ClassNamingImpl replacePackage(String from, String to) {
        pkgPairs.add(Tuple2.of(from, to));
        return this;
    }

    public ClassNamingImpl replacePackages(Collection<Tuple2<String, String>> rules) {
        pkgPairs.addAll(rules);
        return this;
    }

    public ClassNamingImpl replaceSuffix(String from, String to) {
        suffixPairs.add(Tuple2.of(from, to));
        return this;
    }

    public ClassNamingImpl replaceSuffixes(Collection<Tuple2<String, String>> rules) {
        suffixPairs.addAll(rules);
        return this;
    }

    private String fixName(String name) {
        if (name.indexOf('$') >= 0) {
            return Stream.of(StringUtils.split(name, '$'))
                    .map(this::fixName)
                    .collect(Collectors.joining());
        }

        val buf = new StringBuilder();
        if (StringUtils.isNotEmpty(prefix)) {
            buf.append(prefix);
        }
        for (val pair : suffixPairs) {
            if (!name.endsWith(pair.a())) {
                continue;
            }
            name = StringUtils.removeEnd(name, pair.a())
                    + pair.b();
        }

        buf.append(name);

        if (StringUtils.isNotEmpty(suffix)) {
            buf.append(suffix);
        }
        return buf.toString();
    }

    private String fixPkg(String pkg) {
        for (val pair : pkgPairs) {
            if (!pkg.startsWith(pair.a())) {
                continue;
            }
            pkg = pair.b()
                    + StringUtils.removeStart(pkg, pair.a());
        }
        return pkg;
    }

    @Override
    public String resolve(String origin) {
        val direct = mapping.get(origin);
        if (StringUtils.isNotEmpty(direct)) {
            return direct;
        }

        if (origin.indexOf('.') < 0) {
            return fixName(origin);
        }

        String pkg = CodeUtils.pkg(origin) + '.';
        String name = CodeUtils.classSimpleName(origin);
        return fixPkg(pkg)
                + fixName(name);
    }
}
