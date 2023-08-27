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
