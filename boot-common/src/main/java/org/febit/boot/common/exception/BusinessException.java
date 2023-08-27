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
package org.febit.boot.common.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.febit.lang.protocol.IResponse;

@Getter
public class BusinessException extends RuntimeException {

    protected final int status;
    protected final String code;

    public BusinessException(String code, String message) {
        this(code, message, null);
    }

    public BusinessException(int status, String code, String message) {
        this(status, code, message, null);
    }

    public BusinessException(String code, String message, @Nullable Throwable cause) {
        this(500, code, message, cause);
    }

    public BusinessException(int status, String code, String message, @Nullable Throwable cause) {
        super(message, cause, true, false);
        this.status = status;
        this.code = code;
    }

    public <T> IResponse<T> toResponse() {
        return IResponse.failed(this.status, this.code, this.getMessage());
    }

    @Override
    public String getLocalizedMessage() {
        return '[' + this.code + "] " + this.getMessage();
    }
}
