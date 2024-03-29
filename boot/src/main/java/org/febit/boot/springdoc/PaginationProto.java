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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@Data
@ParameterObject
public class PaginationProto {

    @Min(1)
    @Parameter(
            in = ParameterIn.QUERY,
            description = "The number of the page, start from 1.",
            schema = @Schema(type = "integer", defaultValue = "1", example = "1")
    )
    private Integer page;

    @Min(1)
    @Parameter(
            in = ParameterIn.QUERY,
            description = "The size of the page to be returned",
            schema = @Schema(type = "integer", example = "20")
    )
    private Integer size;

    @Parameter(
            in = ParameterIn.QUERY,
            description = "Default sort order",
            schema = @Schema(type = "string", defaultValue = "desc", example = "desc")
    )
    private List<String> order;

    @Parameter(
            in = ParameterIn.QUERY,
            description = "Sorting criteria in the format: property(,asc|desc). "
                    + "Sort order is according to param 'order' if absent. "
                    + "Multiple sort criteria are supported.",
            name = "sort",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    private List<String> sort;

}
