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
package org.febit.boot.auth.web;

import org.febit.boot.auth.AuthSubject;
import org.febit.lang.protocol.IResponse;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;

public interface WebAuthHandler<T extends AuthSubject> {

    IResponse<AuthSubject> verify(WebRequest request, Method handler);
}
