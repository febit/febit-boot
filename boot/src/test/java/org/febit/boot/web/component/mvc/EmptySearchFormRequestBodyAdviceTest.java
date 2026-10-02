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
package org.febit.boot.web.component.mvc;

import org.febit.common.jooq.SearchForm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmptySearchFormRequestBodyAdviceTest {

    private final EmptySearchFormRequestBodyAdvice advice = new EmptySearchFormRequestBodyAdvice();

    static class TestSearchForm implements SearchForm {
    }

    @Test
    void supportsSearchFormClass() {
        assertThat(advice.supports(null, TestSearchForm.class, null)).isTrue();
    }

    @Test
    void doesNotSupportNonSearchFormClass() {
        assertThat(advice.supports(null, String.class, null)).isFalse();
    }

    @Test
    void doesNotSupportNonClassType() {
        assertThat(advice.supports(null, mock(Type.class), null)).isFalse();
    }

    @Test
    void returnsBodyWhenNotNull() {
        var body = new TestSearchForm();
        assertThat(advice.handleEmptyBody(body, null, null, TestSearchForm.class, null)).isSameAs(body);
    }

    @Test
    void instantiatesSearchFormWhenBodyNull() {
        var result = advice.handleEmptyBody(null, null, null, TestSearchForm.class, null);
        assertThat(result).isInstanceOf(TestSearchForm.class);
    }

    @Test
    void returnsNullWhenBodyNullAndTargetNotClass() {
        Type nonClass = mock(Type.class);
        assertThat(advice.handleEmptyBody(null, null, null, nonClass, null)).isNull();
    }
}
