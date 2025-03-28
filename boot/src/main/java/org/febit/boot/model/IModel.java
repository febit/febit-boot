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

import org.febit.boot.util.Models;
import org.febit.lang.annotation.NonNullApi;

import java.util.function.Supplier;

@NonNullApi
public interface IModel<T> {

    default T to(Supplier<T> supplier) {
        return Models.map(this, supplier.get());
    }

    default T to(T target) {
        return Models.map(this, target);
    }

    default void merge(T source) {
        Models.map(source, this);
    }
}
