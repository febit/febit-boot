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
package org.febit.boot.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Generic errors.
 *
 * @see IErrors
 */
@Getter
@RequiredArgsConstructor
public enum Errors implements IErrors {

    // 400
    BAD_REQUEST(400, Logger.DEBUG,
            "Generic bad request, because of a problem on the client side"
    ),
    ILLEGAL_ARG(400, Logger.DEBUG,
            "Request passed an illegal argument"
    ),
    CONSTRAINT_UNIQUE(400, Logger.WARN,
            "Failure to pass unique value constraint check"
    ),
    CONSTRAINT_INTEGRITY(400, Logger.WARN,
            "An integrity constraint (foreign/primary/unique key) has been violated"
    ),

    UNAUTHORIZED(401, Logger.INFO,
            "Unauthorized"
    ),
    FORBIDDEN(403, Logger.INFO,
            "Forbidden"
    ),
    NOT_FOUND(404, Logger.DEBUG,
            "Resource not found"
    ),

    NOT_SUPPORTED(500, Logger.WARN,
            "Planned API or method is not supported for now"
    ),
    SYSTEM(500, Logger.WARN,
            "Generic system error, because of a problem on the server side"
    ),
    ;

    private final int status;
    private final ILogger logger;
    private final String desc;
}
