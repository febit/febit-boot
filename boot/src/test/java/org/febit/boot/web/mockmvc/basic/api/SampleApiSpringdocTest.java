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
package org.febit.boot.web.mockmvc.basic.api;

import org.febit.boot.web.mockmvc.basic.BaseMvcTest;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.febit.boot.web.mockmvc.JsonPathUtils.PATH_200_JSON_SCHEMA_REF;
import static org.febit.boot.web.mockmvc.JsonPathUtils.j;
import static org.febit.boot.web.mockmvc.JsonPathUtils.jsonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SampleApiSpringdocTest extends BaseMvcTest {

    @Autowired
    MockMvc mockMvc;

    ResultActions result() throws Exception {
        return mockMvc.perform(get(Constants.DEFAULT_API_DOCS_URL))
                .andExpect(status().isOk());
    }

    @Test
    void requireById() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/samples/{id}'].get";

        result.andExpect(j(prefix + ".tags[0]", "Sample API"))
                .andExpect(j(prefix + "['x-method-name']", "requireById"))
                .andExpect(j(
                        prefix + PATH_200_JSON_SCHEMA_REF,
                        "#/components/schemas/IResponse<SampleVO>"
                ));
    }

    @Test
    void search() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/samples/search'].post";

        var param = prefix + ".parameters[?(@.name=='page')]";
        result.andExpect(j(param + ".name", "page"))
                .andExpect(j(param + ".in", "query"))
                .andExpect(j(param + ".required", false))
                .andExpect(j(param + ".schema.minimum", 1))
                .andExpect(j(param + ".schema.type", "integer"))
        ;

        param = prefix + ".parameters[?(@.name=='size')]";
        result.andExpect(j(param + ".name", "size"))
                .andExpect(j(param + ".in", "query"))
                .andExpect(j(param + ".required", false))
                .andExpect(j(param + ".schema.minimum", 1))
                .andExpect(j(param + ".schema.type", "integer"))
        ;
        param = prefix + ".parameters[?(@.name=='order')]";
        result.andExpect(j(param + ".name", "order"))
                .andExpect(j(param + ".in", "query"))
                .andExpect(j(param + ".required", false))
                .andExpect(j(param + ".schema.type", "string"))
                .andExpect(j(param + ".schema.default", "desc"))
        ;

        param = prefix + ".parameters[?(@.name=='sort')]";
        result.andExpect(j(param + ".name", "sort"))
                .andExpect(j(param + ".in", "query"))
                .andExpect(j(param + ".required", false))
                .andExpect(j(param + ".schema.type", "array"))
                .andExpect(j(param + ".schema.items.type", "string"))
        ;

        result.andExpect(j(prefix + ".requestBody.required", true))
        ;

        result.andExpect(j(
                prefix + PATH_200_JSON_SCHEMA_REF,
                "#/components/schemas/IResponse<Page<SampleVO>>"
        ))
        ;
    }

    @Test
    void deleteByIds() throws Exception {
        var result = result();
        var prefix = "$.paths['/api/v1/samples/by-ids/{ids}'].delete";

        var param = prefix + ".parameters[?(@.name=='ids')]";
        result.andExpect(j(param + ".name", "ids"))
                .andExpect(j(param + ".in", "path"))
                .andExpect(j(param + ".required", true))
                .andExpect(j(param + ".schema.type", "array"))
                .andExpect(j(param + ".schema.items.type", "integer"))
        ;

        result.andExpect(jsonPath(prefix + ".requestBody.required").doesNotExist())
        ;
    }

    @Test
    void sampleResponseSchema() throws Exception {
        var result = result();
        var prefix = "$.components.schemas['IResponse<SampleVO>']";

        //result.andExpect(j(prefix + ".type", "object"));

        var props = prefix + ".properties";
        result.andExpect(j(props + ".data['$ref']", "#/components/schemas/SampleVO"))
        ;
    }

    @Test
    void sampleSearchFormSchema() throws Exception {
        var result = result();
        var prefix = "$.components.schemas['SampleSearchForm']";

        // result.andExpect(j(prefix + ".type", "object"));

        var props = prefix + ".properties";
        result.andExpect(j(props + ".q.type", "string"))
                .andExpect(j(props + ".q['x-class']", "String"))
                .andExpect(j(props + ".q['x-search-filter']", "KEYWORD"))
                .andExpect(j(props + ".id.type", "integer"))
                .andExpect(j(props + ".id['x-class']", "Integer"))
                .andExpect(j(props + ".id['x-search-filter']", "EQ"))
                .andExpect(j(props + ".name.type", "string"))
                .andExpect(j(props + ".name['x-class']", "String"))
                .andExpect(j(props + ".name['x-search-filter']", "CONTAINS"))
                .andExpect(j(props + ".ids.type", "array"))
                .andExpect(j(props + ".ids.items.type", "integer"))
                .andExpect(j(props + ".ids.items.['x-class']", "Integer"))
                .andExpect(j(props + ".ids['x-class']", "List"))
                .andExpect(j(props + ".ids['x-search-filter']", "IN"))
        ;
    }

    @Test
    void sampleSchema() throws Exception {
        var result = result();
        var prefix = "$.components.schemas['SampleVO']";

        // result.andExpect(j(prefix + ".type", "object"));

        var props = prefix + ".properties";
        result.andExpect(j(props + ".id.type", "integer"))
                .andExpect(j(props + ".id.format", "int64"))
                .andExpect(j(props + ".id['x-class']", "Long"))
        ;
        result.andExpect(j(props + ".status.type", "string"))
                .andExpect(jsonList(props + ".status['enum']", List.of(
                        "OK", "FAILED"
                )))
                .andExpect(j(
                        props + ".status['x-enum-full-class']",
                        "org.febit.boot.web.mockmvc.basic.model.SampleStatus"
                ))
                .andExpect(j(props + ".status['x-enum-class']", "SampleStatus"))
                .andExpect(j(props + ".status['x-class']", "SampleStatus"))
        ;
        result.andExpect(j(props + ".scope.type", "string"))
                .andExpect(jsonList(props + ".scope.enum", List.of(
                        "SESSION", "REQUEST"
                )))
        ;
        result.andExpect(j(props + ".category.type", "integer"))
                .andExpect(jsonList(props + ".category.enum", List.of(
                        500, 600
                )))
        ;
    }
}
