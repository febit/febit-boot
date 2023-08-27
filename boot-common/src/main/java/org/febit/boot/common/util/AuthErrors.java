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
 * Auth errors.
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrors implements IErrors {

    INVALID_GRANT(400, Logger.WARN,
            "auth.unauthorized.grant.invalid"
    ),
    UNSUPPORTED_GRANT_TYPE(400, Logger.WARN,
            "auth.unauthorized.grant.unsupported"
    ),

    UNAUTHORIZED(401, Logger.INFO,
            "auth.unauthorized"
    ),
    INVALID_TOKEN(401, Logger.INFO,
            "auth.unauthorized.token.invalid"
    ),
    TOKEN_EXPIRED(401, Logger.INFO,
            "auth.unauthorized.token.expired"
    ),
    FORBIDDEN(403, Logger.INFO,
            "auth.forbidden"
    ),
    FORBIDDEN_NO_PERMISSION(403, Logger.INFO,
            "auth.forbidden.no-permission"
    ),
    ;

    private final int status;
    private final ILogger logger;
    private final String code;
}
