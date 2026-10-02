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

import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ResponseUtilsTest {

    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        response = mock(ServerHttpResponse.class);
    }

    @Test
    void noOpWhenBodyNull() {
        ResponseUtils.setStatus(response, null);
        verifyNoInteractions(response);
    }

    @Test
    void noOpWhenStatusUnmapped() {
        var body = IResponse.failed(700, "X", "msg");
        ResponseUtils.setStatus(response, body);
        verifyNoInteractions(response);
    }

    @TableTest("""
            scenario        | code
            ok              | 200
            not found       | 404
            internal error  | 500
            bad request     | 400
        """)
    void setsMappedStatus(int code) {
        var body = IResponse.failed(code, "X", "msg");
        ResponseUtils.setStatus(response, body);
        var captured = forClass(HttpStatus.class);
        verify(response).setStatusCode(captured.capture());
        assertThat(captured.getValue().value()).isEqualTo(code);
    }
}
