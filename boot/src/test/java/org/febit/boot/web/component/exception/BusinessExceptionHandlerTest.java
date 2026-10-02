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
package org.febit.boot.web.component.exception;

import org.febit.lang.protocol.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionHandlerTest {

    private final BusinessExceptionHandler handler = new BusinessExceptionHandler();

    @Test
    void handlerReturnsResponseFromException() {
        var ex = new BusinessException(409, "CONFLICT", "conflict detail");
        var resp = handler.handler(ex);
        assertThat(resp.isFailed()).isTrue();
        assertThat(resp.getStatus()).isEqualTo(409);
        assertThat(resp.getMessage()).isEqualTo("conflict detail");
    }

    // BusinessException 以 writableStackTrace=false 构造，getStackTrace() 恒为空，
    // 故 shortenStackTrace 对真实异常恒为空操作。下面用可控栈的测试替身驱动其真实算法。
    @Test
    void shortenStackTraceTruncatesBeforeExcludedFrame() throws Exception {
        var ex = new StackedException(new StackTraceElement[]{
                new StackTraceElement("com.example.Foo", "foo", "Foo.java", 1),
                new StackTraceElement("java.lang.String", "valueOf", "String.java", 100),
        });

        shortenStackTrace(ex);

        assertThat(ex.getStackTrace()).hasSize(1);
        assertThat(ex.getStackTrace()[0].getClassName()).isEqualTo("com.example.Foo");
    }

    @Test
    void shortenStackTraceLeavesStackWhenNoExcludedFrame() throws Exception {
        var ex = new StackedException(new StackTraceElement[]{
                new StackTraceElement("com.example.Foo", "foo", "Foo.java", 1),
                new StackTraceElement("org.febit.Bar", "bar", "Bar.java", 2),
        });

        shortenStackTrace(ex);

        assertThat(ex.getStackTrace()).hasSize(2);
    }

    @Test
    void shortenStackTraceLeavesEmptyStackUntouched() throws Exception {
        var ex = new StackedException(new StackTraceElement[0]);

        shortenStackTrace(ex);

        assertThat(ex.getStackTrace()).isEmpty();
    }

    @Test
    void handlerCatchesStackShortenFailure() {
        var ex = new StackedException(new StackTraceElement[]{null});

        var resp = handler.handler(ex);

        assertThat(resp.isFailed()).isTrue();
    }

    private static void shortenStackTrace(BusinessException ex) throws Exception {
        Method m = BusinessExceptionHandler.class.getDeclaredMethod("shortenStackTrace", BusinessException.class);
        m.setAccessible(true);
        m.invoke(null, ex);
    }

    // 测试替身：重写 getStackTrace/setStackTrace 以可控数组驱动 shortenStackTrace 的真实算法
    // （真实 BusinessException 因 writableStackTrace=false 其 getStackTrace 恒为空）。
    private static final class StackedException extends BusinessException {
        private StackTraceElement[] stack;

        StackedException(StackTraceElement[] stack) {
            super(400, "X", "msg");
            this.stack = stack;
        }

        @Override
        public StackTraceElement[] getStackTrace() {
            return stack;
        }

        @Override
        public void setStackTrace(StackTraceElement[] stack) {
            this.stack = stack;
        }
    }
}
