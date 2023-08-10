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
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.feign.FeignUtils;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.JacksonWrapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@RequiredArgsConstructor
public abstract class BaseResponseDecoder implements Decoder {

    public static final String OK = "OK";
    public static final String FAILED = "FAILED";
    public static final String NO_MSG = "NO MESSAGE";

    protected final JacksonWrapper jackson;
    protected final Decoder delegate;

    @Nullable
    protected Map<String, Object> parse(@Nullable String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        if (raw.charAt(0) != '{') {
            return null;
        }
        return this.jackson.parseToNamedMap(raw);
    }

    protected IResponse<Object> onStatus204() {
        return IResponse.success(204, OK, null, null);
    }

    @Nullable
    protected abstract String resolveMessage(int status, @Nullable Map<String, Object> body);

    protected IResponse<Object> onError(int status, @Nullable Map<String, Object> body) {
        var message = resolveMessage(status, body);
        return IResponse.failed(status, FAILED, message == null ? NO_MSG : message);
    }

    protected IResponse<Object> onSuccess(int status, @Nullable Map<String, Object> body, Type innerType) {
        var innerBean = this.jackson.to(body, innerType);
        return IResponse.success(status, OK, null, innerBean);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        var innerType = FeignUtils.getFirstTypeArgumentIfMatch(type, IResponse.class);
        if (innerType == null) {
            return this.delegate.decode(response, type);
        }

        var status = response.status();
        if (status == 204) {
            return onStatus204();
        }

        var body = parse(
                FeignUtils.readUtf8(response)
        );
        if (status >= 400) {
            return onError(status, body);
        }
        return onSuccess(status, body, innerType);
    }
}

