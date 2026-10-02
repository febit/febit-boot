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
package org.febit.boot.web.component;

import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonCustomizerTest {

    @Test
    void excludesStatusField() throws Exception {
        var json = mapper().writeValueAsString(IResponse.failed(404, "NF", "msg"));
        assertThat(json).doesNotContain("\"status\"");
    }

    @Test
    void omitsNullDataField() throws Exception {
        var json = mapper().writeValueAsString(IResponse.failed(404, "NF", "msg"));
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    void includesNonNullDataField() throws Exception {
        var json = mapper().writeValueAsString(IResponse.ok("payload"));
        assertThat(json).contains("\"data\"").contains("payload");
    }

    @Test
    void ordersSuccessCodeMessage() throws Exception {
        var json = mapper().writeValueAsString(IResponse.failed(404, "NOT_FOUND", "missing"));

        int iSuccess = json.indexOf("\"success\"");
        int iCode = json.indexOf("\"code\"");
        int iMessage = json.indexOf("\"message\"");
        assertThat(iSuccess).isGreaterThanOrEqualTo(0);
        assertThat(iCode).isGreaterThan(iSuccess);
        assertThat(iMessage).isGreaterThan(iCode);
    }

    private static JsonMapper mapper() {
        var builder = JsonMapper.builder();
        new JacksonCustomizer().customize(builder);
        return builder.build();
    }
}
