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
package org.febit.boot.devkit.jooq.gradle;

import org.febit.boot.devkit.flyway.gradle.model.JdbcOption;
import org.febit.lang.util.TypeParameters;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import java.util.Objects;

public interface JdbcProvider<P> extends AutoCloseable {

    default void afterEvaluate(Project project) {
    }

    JdbcOption prepare(P params);

    @Override
    void close();

    default JdbcConnection open(
            ObjectFactory objectFactory
    ) {
        var paramsType = TypeParameters.forType(getClass())
                .resolve(JdbcProvider.class, 0)
                .get();

        Objects.requireNonNull(paramsType, "Can't resolve type of JdbcProvider parameters");

        @SuppressWarnings("unchecked")
        var params = (P) (paramsType == Object.class
                ? new Object()
                : objectFactory.newInstance(paramsType));

        var option = prepare(params);
        return JdbcConnectionImpl.create(
                option, this
        );
    }

}
