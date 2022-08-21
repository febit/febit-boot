package org.febit.jooq.codegen;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.febit.util.Lists;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.ServiceLoader;

@Slf4j
@UtilityClass
class SpiUtils {

    static <T> List<T> load(Class<T> type) {

        List<T> impls = Lists.collect(ServiceLoader.load(type));
        AnnotationAwareOrderComparator.sort(impls);

        if (log.isInfoEnabled()) {
            var spi = type.getSimpleName();
            for (var impl : impls) {
                log.info("Loaded SPI [{}]: {}", spi, impl.getClass());
            }
        }

        return impls;
    }
}
