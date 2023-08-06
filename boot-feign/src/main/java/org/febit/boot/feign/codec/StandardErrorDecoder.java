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
package org.febit.boot.feign.codec;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.feign.FeignUtils;
import org.febit.boot.feign.ResponseErrorException;
import org.febit.boot.feign.ResponseSnapshot;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class StandardErrorDecoder implements ErrorDecoder {

    /**
     * RetryAfterDecoder is not accessible, use {@linkplain Default} instead.
     */
    private final ErrorDecoder retryAfterErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        var hasRetryAfter = StringUtils.isNotBlank(
                FeignUtils.firstHeader(response, Util.RETRY_AFTER)
        );

        if (hasRetryAfter) {
            return this.retryAfterErrorDecoder.decode(methodKey, response);
        }

        try {
            return new ResponseErrorException(
                    ResponseSnapshot.snapshot(response)
            );
        } catch (IOException e) {
            return e;
        }
    }
}
