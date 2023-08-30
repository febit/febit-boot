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
package org.febit.boot.springdoc;

import lombok.RequiredArgsConstructor;
import org.febit.lang.protocol.Pagination;
import org.springdoc.core.converters.AdditionalModelsConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({
        AdditionalModelsConverter.class
})
@RequiredArgsConstructor
public class PaginationModelConverter implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        AdditionalModelsConverter.replaceParameterObjectWithClass(Pagination.class, PaginationProto.class);
    }
}
