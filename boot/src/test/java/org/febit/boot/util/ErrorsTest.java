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
package org.febit.boot.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorsTest {

    @Test
    void constantsCarryExpectedStatusLoggerAndDesc() {
        record Expect(Errors errors, int status, IErrors.Logger logger, String desc) {
        }
        var expects = new Expect[]{
                new Expect(Errors.BAD_REQUEST, 400, IErrors.Logger.DEBUG,
                        "Generic bad request, because of a problem on the client side"),
                new Expect(Errors.ILLEGAL_ARG, 400, IErrors.Logger.DEBUG,
                        "Request passed an illegal argument"),
                new Expect(Errors.CONSTRAINT_UNIQUE, 400, IErrors.Logger.WARN,
                        "Failure to pass unique value constraint check"),
                new Expect(Errors.CONSTRAINT_INTEGRITY, 400, IErrors.Logger.WARN,
                        "An integrity constraint (foreign/primary/unique key) has been violated"),
                new Expect(Errors.UNAUTHORIZED, 401, IErrors.Logger.INFO, "Unauthorized"),
                new Expect(Errors.FORBIDDEN, 403, IErrors.Logger.INFO, "Forbidden"),
                new Expect(Errors.NOT_FOUND, 404, IErrors.Logger.DEBUG, "Resource not found"),
                new Expect(Errors.NOT_SUPPORTED, 500, IErrors.Logger.WARN,
                        "Planned API or method is not supported for now"),
                new Expect(Errors.SYSTEM, 500, IErrors.Logger.WARN,
                        "Generic system error, because of a problem on the server side"),
        };

        for (var e : expects) {
            assertThat(e.errors.getStatus()).isEqualTo(e.status);
            assertThat(e.errors.getLogger()).isEqualTo(e.logger);
            assertThat(e.errors.getDesc()).isEqualTo(e.desc);
        }
    }

    @Test
    void allConstantsHaveNonBlankDescAndKnownStatus() {
        for (var errors : Errors.values()) {
            assertThat(errors.getDesc()).isNotBlank();
            assertThat(errors.getStatus()).isIn(400, 401, 403, 404, 500);
        }
    }
}
