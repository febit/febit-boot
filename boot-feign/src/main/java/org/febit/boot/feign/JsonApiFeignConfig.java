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
package org.febit.boot.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import feign.Feign;
import feign.QueryMapEncoder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Feign config for json api.
 */
public class JsonApiFeignConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.standard(new ObjectMapper());
    }

    @Bean
    public JacksonWrapper jacksonWrapper(ObjectMapper mapper) {
        return JacksonUtils.wrap(mapper);
    }

    @Bean
    public HttpMessageConverters httpMessageConverters(ObjectMapper mapper) {
        return new HttpMessageConverters(
                new MappingJackson2HttpMessageConverter(mapper)
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return Decoders.standardErrorDecoder();
    }

    @Bean
    public Encoder encoder(ObjectMapper mapper) {
        return Encoders.chain(mapper);
    }

    @Bean
    public Decoder decoder(ObjectMapper mapper) {
        return Decoders.chain(
                mapper,
                Decoders::responseStatusDecoder,
                Decoders::optionalDecoder
        );
    }

    @Bean
    public Feign.Builder feignBuilder(Retryer retryer, Decoder decoder) {
        return Feign.builder()
                .retryer(retryer)
                .invocationHandlerFactory(InvocationHandlerImpl.factory(decoder));
    }

    @Bean
    public Contract contract(ConversionService feignConversionService) {
        return SpringMvcExtraContract.builder()
                .conversionService(feignConversionService)
                .build();
    }

    @Bean
    public QueryMapEncoder queryMapEncoder(JacksonWrapper jackson) {
        return Encoders.queryMapEncoder(jackson);
    }

    @Bean
    public RequestInterceptor standardJsonApiBasicRequestInterceptor() {
        return this::onRequest;
    }

    private void onRequest(RequestTemplate request) {
        request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

}
