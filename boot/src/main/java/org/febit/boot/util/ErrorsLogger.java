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

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
class ErrorsLogger {

    static final Logger LOGGER = LoggerFactory.getLogger(ErrorsLogger.class);

    static boolean disabled() {
        return false;
    }

    @SuppressWarnings("unused")
    static void discard(String msg) {
        // Nothing to do
    }

    @SuppressWarnings("unused")
    static void discard(String msg, Exception ex) {
        // Nothing to do
    }
}
