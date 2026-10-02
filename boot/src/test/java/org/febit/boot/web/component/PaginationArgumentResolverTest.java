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
package org.febit.boot.web.component;

import org.febit.lang.protocol.BusinessException;
import org.febit.lang.protocol.Pagination;
import org.febit.lang.protocol.Sort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaginationArgumentResolverTest {

    @TableTest("""
            scenario            | input     | defaultDirection | expected
            null uses default   |           | DESC             | DESC
            blank uses default  | " "       | ASC              | ASC
            upper ASC           | ASC       | DESC             | ASC
            lower asc           | asc       | DESC             | ASC
            spaced desc         | "  desc " | ASC              | DESC
            """)
    void parseDirection(String input, Sort.Direction defaultDirection, Sort.Direction expected) {
        assertThat(PaginationArgumentResolver.parseDirection(input, defaultDirection))
                .isEqualTo(expected);
    }

    @Test
    void parseDirectionRejectsUnknown() {
        assertThrows(BusinessException.class,
                () -> PaginationArgumentResolver.parseDirection("sideways", Sort.Direction.DESC));
    }

    @TableTest("""
            scenario          | raw          | defaultDir | expectedDir
            default direction | "id"         | ASC        | ASC
            suffix desc       | "id,desc"    | ASC        | DESC
            spaced upper asc  | " id , ASC " | DESC       | ASC
            """)
    void parseOrder(String raw, Sort.Direction defaultDir, Sort.Direction expectedDir) {
        assertThat(PaginationArgumentResolver.parseOrder(raw, defaultDir))
                .isEqualTo(Sort.of("id", expectedDir));
    }

    @Test
    void parseOrderReturnsNullWhenBlank() {
        assertThat(PaginationArgumentResolver.parseOrder("  ", Sort.Direction.ASC)).isNull();
        assertThat(PaginationArgumentResolver.parseOrder("", Sort.Direction.ASC)).isNull();
    }

    @Test
    void parseOrdersReturnsEmptyForNullOrEmpty() {
        assertThat(PaginationArgumentResolver.parseOrders(null, Sort.Direction.ASC)).isEmpty();
        assertThat(PaginationArgumentResolver.parseOrders(new String[0], Sort.Direction.ASC)).isEmpty();
    }

    @Test
    void parseOrdersSplitsByPipeAndIgnoresBlanks() {
        var raws = new String[]{"id,desc", "", " name | age,asc ", "  "};
        assertThat(PaginationArgumentResolver.parseOrders(raws, Sort.Direction.ASC))
                .containsExactly(
                        Sort.of("id", Sort.Direction.DESC),
                        Sort.of("name", Sort.Direction.ASC),
                        Sort.of("age", Sort.Direction.ASC)
                );
    }

    @Test
    void parseOrdersAppliesDefaultDirection() {
        var raws = new String[]{"id", "name,desc"};
        assertThat(PaginationArgumentResolver.parseOrders(raws, Sort.Direction.DESC))
                .containsExactly(
                        Sort.of("id", Sort.Direction.DESC),
                        Sort.of("name", Sort.Direction.DESC)
                );
    }

    @TableTest("""
            scenario        | raw       | defaultValue | expected
            null default    |           | 1            | 1
            trimmed parse   | "  10  "  | 20           | 10
            plain           | "5"       | 0            | 5
            negative        | "-3"      | 0            | -3
            """)
    void getIntParameter(String raw, int defaultValue, int expected) {
        var request = mock(WebRequest.class);
        when(request.getParameter("page")).thenReturn(raw);
        assertThat(PaginationArgumentResolver.getIntParameter(request, "page", defaultValue))
                .isEqualTo(expected);
    }

    @Test
    void getIntParameterRejectsNonNumeric() {
        var request = mock(WebRequest.class);
        when(request.getParameter("page")).thenReturn("abc");
        assertThrows(BusinessException.class,
                () -> PaginationArgumentResolver.getIntParameter(request, "page", 1));
    }

    @Nested
    class SupportsParameter {
        @Test
        void pagination() throws Exception {
            assertThat(supports("pagination", Pagination.class)).isTrue();
        }

        @Test
        void nonPagination() throws Exception {
            assertThat(supports("string", String.class)).isFalse();
        }

        @Test
        void paginationSubtype() throws Exception {
            assertThat(supports("subtype", SubPagination.class)).isFalse();
        }

        private static boolean supports(String methodName, Class<?> paramType) throws Exception {
            var resolver = new PaginationArgumentResolver();
            var method = Sample.class.getDeclaredMethod(methodName, paramType);
            return resolver.supportsParameter(new MethodParameter(method, 0));
        }
    }

    @Test
    void resolveArgumentReadsPageSizeOrderAndSorts() {
        var resolver = new PaginationArgumentResolver();
        var request = mock(NativeWebRequest.class);
        when(request.getParameter("page")).thenReturn("3");
        when(request.getParameter("size")).thenReturn("50");
        when(request.getParameter("order")).thenReturn("asc");
        when(request.getParameterValues("sort")).thenReturn(new String[]{"name", "age,desc"});

        var pagination = resolver.resolveArgument(null, null, request, null);

        assertThat(pagination.getPage()).isEqualTo(3);
        assertThat(pagination.getSize()).isEqualTo(50);
        assertThat(pagination.getSorts())
                .containsExactly(Sort.of("name", Sort.Direction.ASC), Sort.of("age", Sort.Direction.DESC));
    }

    @Test
    void resolveArgumentUsesDefaultsWhenParamsAbsent() {
        var resolver = new PaginationArgumentResolver();
        var request = mock(NativeWebRequest.class);

        var pagination = resolver.resolveArgument(null, null, request, null);

        assertThat(pagination.getPage()).isEqualTo(1);
        assertThat(pagination.getSize()).isEqualTo(2000);
        assertThat(pagination.getSorts()).isEmpty();
    }

    @SuppressWarnings("unused")
    static class Sample {
        void pagination(Pagination p) {
        }

        void string(String s) {
        }

        void subtype(SubPagination p) {
        }
    }

    static class SubPagination extends Pagination {
    }
}
