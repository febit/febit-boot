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
package org.febit.boot.web.mockmvc;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@UtilityClass
public class JsonPathUtils {

    public static final String PATH_200_JSON_SCHEMA_REF = ".responses['200']"
            + ".content['application/json']"
            + ".schema['$ref']";

    public static ResultMatcher j(String p, Object v) {
        return jsonPath(p).value(v);
    }

    public static ResultMatcher jsonList(String p, List<?> v) {
        return jsonPath(p).value(Matchers.is(v));
    }
}
