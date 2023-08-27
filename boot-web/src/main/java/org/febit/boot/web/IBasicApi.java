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
package org.febit.boot.web;

import org.febit.lang.protocol.IResponse;

public interface IBasicApi {

    default <T> IResponse<T> ok() {
        return IResponse.success();
    }

    default <T> IResponse<T> ok(T data) {
        return IResponse.success(data);
    }

    default <T> IResponse<T> created() {
        return IResponse.success(201, null);
    }

    default <T> IResponse<T> accepted() {
        return IResponse.success(202, null);
    }
}
