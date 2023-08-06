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

import feign.Request;
import feign.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Snapshot of Response.
 *
 * @see Response
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseSnapshot implements Serializable {

    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final byte[] body;
    private final Request.ProtocolVersion protocolVersion;
    private final Request request;

    public static ResponseSnapshot snapshot(Response resp) throws IOException {
        byte[] body = FeignUtils.read(resp);
        return new ResponseSnapshot(
                resp.status(),
                resp.reason(),
                resp.headers(),
                body,
                resp.protocolVersion(),
                resp.request()
        );
    }

    public Response toResponse() {
        return Response.builder()
                .request(this.request)
                .protocolVersion(this.protocolVersion)
                .status(this.status)
                .reason(this.reason)
                .headers(this.headers)
                .body(this.body)
                .build();
    }

}
