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
package org.febit.boot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import java.util.Objects;

public interface INamed {

    String getName();

    @JsonIgnore
    default boolean isNameEquals(@Nullable String name) {
        return Objects.equals(getName(), name);
    }

    @JsonIgnore
    default boolean isNameEquals(@Nullable INamed other) {
        if (other == null) {
            return false;
        }
        return isNameEquals(other.getName());
    }
}
