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
package org.febit.boot.devkit.jooq.runtime;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.devkit.jooq.runtime.spi.Aware;
import org.febit.lang.util.Lists;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.ServiceLoader;

@Slf4j
@UtilityClass
class SpiUtils {

    static <T> List<T> load(Class<T> type, JooqGeneratorStrategy strategy) {
        var spies = load(type);
        for (T spi : spies) {
            if (spi instanceof Aware.Strategy aware) {
                aware.setGeneratorStrategy(strategy);
            }
        }
        return spies;
    }

    static <T> List<T> load(Class<T> type) {
        List<T> impls = Lists.collect(ServiceLoader.load(type));
        SpiOrderComparator.sort(impls);

        if (log.isInfoEnabled()) {
            var spi = type.getSimpleName();
            for (var impl : impls) {
                log.info("Loaded SPI [{}] in order: {}", spi, impl.getClass());
            }
        }
        return impls;
    }

    public static class SpiOrderComparator extends AnnotationAwareOrderComparator {

        @Override
        protected Integer findOrder(Object obj) {
            var order = super.findOrder(obj);
            if (order != null) {
                return order;
            }
            // Default as Normal Priority
            return 0;
        }
    }
}
