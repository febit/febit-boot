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
package org.febit.boot.feign.test;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.QueryMapEncoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestApi<T> {

    private final Class<T> apiType;

    public static <E> TestApi<E>.Builder builder(Class<E> apiType) {
        return new TestApi<>(apiType).builder();
    }

    private static FeignContextImpl createContext(Class<?> apiType) {
        var anno = apiType.getAnnotation(FeignClient.class);
        var profile = anno.contextId();

        var context = new FeignContextImpl(profile);
        context.setConfigurations(List.of(new SpecificationImpl(
                profile,
                ArrayUtils.addAll(anno.configuration(), ExtraClientConfig.class)
        )));
        return context;
    }

    private static Feign.Builder resolveBuilder(ApplicationContext app) {
        var context = app.getBean(FeignContextImpl.class);
        var builder = context.getInstance(Feign.Builder.class);

        context.injectTo(Client.class, builder::client);
        context.injectTo(Contract.class, builder::contract);
        context.injectTo(Retryer.class, builder::retryer);
        context.injectTo(Encoder.class, builder::encoder);
        context.injectTo(QueryMapEncoder.class, builder::queryMapEncoder);
        context.injectTo(Decoder.class, builder::decoder);
        context.injectTo(ErrorDecoder.class, builder::errorDecoder);

        builder.requestInterceptors(
                context.getInstances(RequestInterceptor.class).values()
        );
        return builder;
    }

    private Builder builder() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked"})
    private void addBean(StaticApplicationContext context, Object bean) {
        context.registerBean((Class<Object>) bean.getClass(), () -> bean);
    }

    @lombok.Builder(
            builderClassName = "Builder",
            builderMethodName = ""
    )
    private T newApi(
            @Singular List<Object> beans,
            @Singular List<Consumer<StaticApplicationContext>> contextCustomizers,
            @Singular List<FeignBuilderCustomizer> feignBuilderCustomizers,
            @Nullable Supplier<Client> clientSupplier,
            @Nullable Logger.Level loggerLevel
    ) {
        var app = new StaticApplicationContext();

        beans.forEach(bean -> addBean(app, bean));
        contextCustomizers.forEach(c -> c.accept(app));

        app.registerBean(FeignContextImpl.class, () -> createContext(apiType));
        app.refresh();

        // API builder
        var builder = resolveBuilder(app);

        // Logger
        builder.logger(new Logger.JavaLogger(apiType));
        builder.logLevel(loggerLevel != null ? loggerLevel : Logger.Level.FULL);

        // Override client if provided
        if (clientSupplier != null) {
            builder.client(clientSupplier.get());
        }

        feignBuilderCustomizers.forEach(c -> c.customize(builder));

        // Build API instance
        var api = builder.build()
                .newInstance(new Target.HardCodedTarget<>(
                        apiType, "http://localhost:8080/"
                ));

        // Register to ApplicationContext
        app.registerBean(apiType, () -> api);

        // Done!
        return (T) api;
    }

    private static class ExtraClientConfig {

        @Bean
        @ConditionalOnMissingBean
        public Client client() {
            var interceptor = new HttpLoggingInterceptor(log::info);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            var okClient = new okhttp3.OkHttpClient.Builder()
                    .addNetworkInterceptor(interceptor)
                    .build();

            return new OkHttpClient(okClient);
        }

        @Bean
        public Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }

        @Bean
        public FeignHttpClientProperties feignHttpClientProperties() {
            return new FeignHttpClientProperties();
        }
    }

    private static class FeignContextImpl extends NamedContextFactory<SpecificationImpl> {

        private final String profile;

        FeignContextImpl(String profile) {
            super(FeignClientsConfiguration.class, "feign", "feign.client.name");
            this.profile = profile;
        }

        public <E> void injectTo(Class<E> type, Consumer<E> consumer) {
            var bean = getInstance(type);
            if (bean != null) {
                consumer.accept(bean);
            }
        }

        public <T> T getInstance(Class<T> type) {
            return getInstance(profile, type);
        }

        public <T> Map<String, T> getInstances(Class<T> type) {
            return getInstances(profile, type);
        }
    }

    @Data
    private static class SpecificationImpl implements NamedContextFactory.Specification {
        private final String name;
        private final Class<?>[] configuration;
    }

}
