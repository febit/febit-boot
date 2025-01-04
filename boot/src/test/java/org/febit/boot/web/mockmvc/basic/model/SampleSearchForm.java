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
package org.febit.boot.web.mockmvc.basic.model;

import lombok.Data;
import org.febit.common.jooq.OrderMappingBy;
import org.febit.common.jooq.SearchForm;

import java.time.Instant;
import java.util.List;

@Data
@OrderMappingBy(SampleOrderMapping.class)
public class SampleSearchForm implements SearchForm {

    @Keyword({
            "name"
    })
    private String q;

    @Equals
    private Integer id;

    @Contains
    private String name;

    @Equals
    private SampleCategory category;

    @Equals
    private SampleStatus status;

    @Equals
    private String createdBy;

    @Equals
    private String updatedBy;

    private Instant createdAt;

    private Instant updatedAt;

    @Contains
    private String description;

    @In("id")
    private List<Integer> ids;

    @In("category")
    private List<SampleCategory> categories;

}
