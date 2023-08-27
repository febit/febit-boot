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
package org.febit.boot.web.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.febit.lang.protocol.IResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ResponseUtils {

    private static class StatusLazyHolder {
        static final Map<Integer, HttpStatus> MAPPING;

        static {
            // Override duplicate keys
            var map = new HashMap<Integer, HttpStatus>();
            for (var status : HttpStatus.values()) {
                map.put(status.value(), status);
            }
            MAPPING = Map.copyOf(map);
        }
    }

    public void setStatus(ServerHttpResponse response, @Nullable IResponse<?> body) {
        if (body == null) {
            return;
        }
        var status = StatusLazyHolder.MAPPING.get(body.getStatus());
        if (status != null) {
            response.setStatusCode(status);
        }
    }
}
