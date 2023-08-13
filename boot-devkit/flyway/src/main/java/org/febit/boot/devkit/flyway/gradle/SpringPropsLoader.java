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
package org.febit.boot.devkit.flyway.gradle;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.util.ArraysUtils;
import org.febit.lang.util.Lists;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;

@UtilityClass
public class SpringPropsLoader {

    public static List<String> resolveList(Properties props, String key, int max) {
        var v = props.getProperty(key);
        if (StringUtils.isNotEmpty(v)) {
            return List.of(v);
        }

        var buf = Lists.<String>ofArrayList();
        for (int i = 0; i < max; i++) {
            v = props.getProperty(key + '[' + i + ']');
            if (v == null) {
                break;
            }
            buf.add(v);
        }
        return List.copyOf(buf);
    }

    public static Properties yaml(String... paths) {
        return yaml(ArraysUtils.collect(paths, Resource[]::new, FileSystemResource::new));
    }

    public static Properties yaml(Resource... resources) {
        var factory = new YamlPropertiesFactoryBean();
        factory.setSingleton(false);
        factory.afterPropertiesSet();

        factory.setResources(resources);
        var props = factory.getObject();
        return props != null ? props : new Properties();
    }

    public static Properties properties(String... paths) {
        return properties(ArraysUtils.collect(paths, Resource[]::new, FileSystemResource::new));
    }

    public static Properties properties(Resource... resources) {
        var factory = new PropertiesFactoryBean();
        factory.setSingleton(false);
        try {
            factory.afterPropertiesSet();
            factory.setLocations(resources);
            var props = factory.getObject();
            return props != null ? props : new Properties();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
