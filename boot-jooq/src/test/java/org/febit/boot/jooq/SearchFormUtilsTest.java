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
package org.febit.boot.jooq;

import lombok.Data;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SearchFormUtilsTest {

    @Data
    static class EmptySearchForm implements SearchForm {
    }

    record EmptyRecordSearchForm() implements SearchForm {
    }

    record RecordSearchForm(
            @Keyword({
                    "name",
                    "title"
            })
            String q,
            @Contains
            String value,
            @NotContains
            String notValue
    ) implements SearchForm {
    }

    @lombok.Builder
    static class StdSearchForm implements SearchForm {
        @Keyword({
                "key", "name", "title"
        })
        String q;
        @Contains
        String contains;
        @NotContains
        String notContains;
        @StartsWith
        String startsWith;
        @EndsWith
        String endsWith;
        @GreaterThan
        Integer gt;
        @GreaterEquals
        Integer ge;
        @LessThan
        Integer lt;
        @LessEquals
        Integer le;
        @Equals
        Integer eq;
        @In
        Integer[] in;
        @NotIn
        List<String> notIn;
    }

    @Test
    void empty() {
        var dsl = mock(DSLContext.class);
        var conditions = new EmptySearchForm().toConditions(dsl);
        assertThat(conditions).isEmpty();

        conditions = SearchFormUtils.listAnnotatedConditions(dsl, new EmptyRecordSearchForm());
        assertThat(conditions).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void record() {
        var dsl = mock(DSLContext.class);
        var conditions = new RecordSearchForm("key", "value", "not").toConditions(dsl);
        assertThat(conditions)
                .hasSize(3)
                .contains(
                        Conditions.keywords("key", Fields.string("name"), Fields.string("title"))
                )
                .contains(
                        Fields.string("value").contains("value"),
                        Fields.string("not_value").notContains("not")
                )
        ;
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void std() {
        var dsl = mock(DSLContext.class);
        var conditions = StdSearchForm.builder()
                .q("key")
                .contains("contains")
                .notContains("notContains")
                .startsWith("startsWith")
                .endsWith("endsWith")
                .gt(1)
                .ge(2)
                .lt(3)
                .le(4)
                .eq(5)
                .in(new Integer[]{1, 2, 3})
                .notIn(List.of("a", "b", "c"))
                .build()
                .toConditions(dsl);

        assertThat(conditions)
                .hasSize(12)
                .contains(
                        Conditions.keywords("key",
                                Fields.string("key"),
                                Fields.string("name"),
                                Fields.string("title")
                        )
                )
                .contains(
                        Fields.string("contains").contains("contains")
                )
                .contains(
                        Fields.string("not_contains").notContains("notContains")
                )
                .contains(
                        Fields.string("starts_with").startsWith("startsWith")
                )
                .contains(
                        Fields.string("ends_with").endsWith("endsWith")
                )
                .contains(
                        Fields.integer("gt").greaterThan(1)
                )
                .contains(
                        Fields.integer("ge").ge(2)
                )
                .contains(
                        Fields.integer("lt").lt(3)
                )
                .contains(
                        Fields.integer("le").le(4)
                )
                .contains(
                        Fields.integer("eq").eq(5)
                )
                .contains(
                        Fields.integer("in").in(1, 2, 3)
                )
                .contains(
                        Fields.string("not_in").notIn("a", "b", "c")
                )
        ;
    }

}
