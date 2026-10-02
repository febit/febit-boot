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
package org.febit.boot.util;

import org.febit.lang.protocol.BusinessException;
import org.febit.lang.protocol.Fallible;
import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IErrorsTest {

    @BeforeEach
    void setEnglishLocale() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }


    @Test
    void normalizeArgsReturnsEmptyForNull() {
        assertThat(Errors.SYSTEM.normalizeArgs(null)).isEmpty();
    }

    @Test
    void normalizeArgsConvertsNumbersToStrings() {
        assertThat(Errors.SYSTEM.normalizeArgs(new Object[]{1, "x", 2L}))
                .containsExactly("1", "x", "2");
    }

    @Test
    void formatReturnsCodeWhenUnresolved() {
        assertThat(Errors.SYSTEM.format("unknown.code", new Object[0])).isEqualTo("unknown.code");
    }

    @Test
    void responseBuildsFailedResponse() {
        var resp = Errors.NOT_FOUND.response("not.found.code");
        assertThat(resp.isFailed()).isTrue();
        assertThat(resp.getStatus()).isEqualTo(404);
        assertThat(resp.getMessage()).isEqualTo("not.found.code");
    }

    @Test
    void exceptionBuildsBusinessException() {
        var ex = Errors.NOT_FOUND.exception("not.found.code");
        assertThat(ex.getStatus()).isEqualTo(404);
        assertThat(ex.getCode()).isEqualTo("NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("not.found.code");
    }

    @Test
    void failedThrowsBusinessException() {
        var ex = assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.failed("bad.code"));
        assertThat(ex.getStatus()).isEqualTo(400);
        assertThat(ex.getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void whenNullThrowsOnNull() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenNull(null, "bad.code"));
    }

    @Test
    void whenNullPassesOnNonNull() {
        Errors.BAD_REQUEST.whenNull("x", "bad.code");
    }

    @Test
    void whenEmptyCollectionThrowsOnEmpty() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenEmpty(List.of(), "bad.code"));
    }

    @Test
    void whenEmptyCollectionPassesOnNonEmpty() {
        Errors.BAD_REQUEST.whenEmpty(List.of("a"), "bad.code");
    }

    @Test
    void whenEmptyCharSequenceThrowsOnEmpty() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenEmpty("", "bad.code"));
    }

    @Test
    void whenEmptyCharSequencePassesOnNonEmpty() {
        Errors.BAD_REQUEST.whenEmpty("x", "bad.code");
    }

    @Test
    void whenBlankThrowsOnBlank() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenBlank("   ", "bad.code"));
    }

    @Test
    void whenBlankPassesOnNonBlank() {
        Errors.BAD_REQUEST.whenBlank("x", "bad.code");
    }

    @Test
    void whenFalseThrowsOnFalse() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenFalse(false, "bad.code"));
    }

    @Test
    void whenFalsePassesOnTrue() {
        Errors.BAD_REQUEST.whenFalse(true, "bad.code");
    }

    @Test
    void whenFailedThrowsOnFailedResponse() {
        var failed = IResponse.failed(500, "X", "msg");
        assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenFailed(failed));
    }

    @Test
    void whenFailedPassesOnSuccessResponse() {
        Errors.SYSTEM.whenFailed(IResponse.ok("data"));
    }

    @Test
    void whenFailedWithPatternThrowsOnFailed() {
        var failed = IResponse.failed(500, "X", "msg");
        var ex = assertThrows(BusinessException.class,
                () -> Errors.SYSTEM.whenFailed(failed, "sys.err"));
        assertThat(ex.getCode()).isEqualTo("SYSTEM");
        assertThat(ex.getMessage()).isEqualTo("sys.err");
    }

    @Test
    void whenFailedExclude404PassesOn404() {
        Errors.SYSTEM.whenFailedExclude404(IResponse.failed(404, "X", "msg"));
    }

    @Test
    void whenFailedExclude404ThrowsOnNon404Failed() {
        var failed = IResponse.failed(500, "X", "msg");
        var ex = assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenFailedExclude404(failed));
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getCode()).isEqualTo("SYSTEM");
    }

    @Test
    void whenFailedExclude404PassesOnSuccess() {
        Errors.SYSTEM.whenFailedExclude404(IResponse.ok("data"));
    }

    @Test
    void whenFailedExclude404WithPatternThrowsOnNon404() {
        var failed = IResponse.failed(500, "X", "msg");
        var ex = assertThrows(BusinessException.class,
                () -> Errors.SYSTEM.whenFailedExclude404(failed, "sys.err"));
        assertThat(ex.getMessage()).isEqualTo("sys.err");
    }

    @Test
    void when404ThrowsOn404() {
        var failed = IResponse.failed(404, "X", "msg");
        assertThrows(BusinessException.class, () -> Errors.SYSTEM.when404(failed, "not.found"));
    }

    @Test
    void when404PassesOnNon404() {
        Errors.SYSTEM.when404(IResponse.failed(500, "X", "msg"), "not.found");
    }

    @Test
    void when404PassesOnSuccess() {
        Errors.SYSTEM.when404(IResponse.ok("data"), "not.found");
    }

    @Test
    void whenExceptionPassesWhenActionSucceeds() {
        Errors.SYSTEM.whenException(() -> {
        }, "sys.err");
    }

    @Test
    void whenExceptionWrapsNonBusinessException() {
        var ex = assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenException(() -> {
            throw new RuntimeException("boom");
        }, "sys.err"));
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getCode()).isEqualTo("SYSTEM");
    }

    @Test
    void whenExceptionRethrowsWrappedBusinessException() {
        var biz = new BusinessException(400, "BAD", "bad msg");
        var exec = new ExecutionException(biz);
        var thrown = assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenException(() -> {
            throw exec;
        }, "sys.err"));
        assertThat(thrown).isSameAs(biz);
    }

    @Test
    void formatReturnsResolvedMessageWithArgs() {
        assertThat(Errors.SYSTEM.format("greeting.name", new Object[]{"World"}))
                .isEqualTo("Hello World");
    }

    @Test
    void formatWithExceptionOverloadReturnsResolvedMessage() {
        assertThat(Errors.SYSTEM.format(new RuntimeException("boom"), "greeting.name", new Object[]{"World"}))
                .isEqualTo("Hello World");
    }

    @Test
    void whenEmptyCollectionThrowsOnNull() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenEmpty((Collection<?>) null, "bad.code"));
    }

    @Test
    void whenEmptyCharSequenceThrowsOnNull() {
        CharSequence cs = null;
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenEmpty(cs, "bad.code"));
    }

    @Test
    void whenBlankThrowsOnNull() {
        assertThrows(BusinessException.class, () -> Errors.BAD_REQUEST.whenBlank(null, "bad.code"));
    }

    // FIXME: a directly-thrown BusinessException is re-wrapped into a new one,
    // losing its original status/code (only ExecutionException-wrapped ones are preserved).
    @Test
    void whenExceptionRethrowsDirectBusinessExceptionAsRewrapped() {
        var biz = new BusinessException(409, "CONFLICT", "conflict");
        var ex = assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenException(() -> {
            throw biz;
        }, "sys.err"));
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getCode()).isEqualTo("SYSTEM");
        assertThat(ex.getMessage()).isEqualTo("sys.err");
    }

    @Test
    void whenFailedOnFalliblePassesWhenNotFailed() {
        Fallible ok = () -> false;
        Errors.SYSTEM.whenFailed(ok, "sys.err");
    }

    @Test
    void whenFailedExclude404WithPatternPassesOn404() {
        Errors.SYSTEM.whenFailedExclude404(IResponse.failed(404, "X", "msg"), "not.found");
    }

    @Test
    void whenFailedExclude404WithPatternPassesOnSuccess() {
        Errors.SYSTEM.whenFailedExclude404(IResponse.ok("data"), "not.found");
    }

    @Test
    void whenExceptionWrapsExecutionExceptionWithNonBusinessCause() {
        var cause = new RuntimeException("inner");
        var exec = new ExecutionException(cause);
        var ex = assertThrows(BusinessException.class, () -> Errors.SYSTEM.whenException(() -> {
            throw exec;
        }, "sys.err"));
        assertThat(ex.getCode()).isEqualTo("SYSTEM");
    }

    @Test
    void whenExceptionDoesNotThrowWhenActionSucceeds() {
        Errors.SYSTEM.whenException(() -> {
            // no-op, success path
        }, "sys.err");
    }

    // 遍历 IErrors.Logger 各档位，使每个档位的 log/log(ex) 两个分支（放行与早返回）均被走到：
    // 放开 ErrorsLogger 到 DEBUG 后，DEBUG/INFO/WARN/ERROR 放行、TRACE/OFF 早返回。
    @Test
    void loggerLevelsCoverBothBranches() {
        var dummy = new IErrors() {
            @Override
            public String name() {
                return "DUMMY";
            }

            @Override
            public int getStatus() {
                return 400;
            }
        };
        for (var level : IErrors.Logger.values()) {
            level.log(dummy, "msg");
            level.log(dummy, "msg", new RuntimeException("boom"));
        }
    }
}
