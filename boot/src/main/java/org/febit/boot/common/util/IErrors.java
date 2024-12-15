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
package org.febit.boot.common.util;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.protocol.BusinessException;
import org.febit.lang.protocol.Fallible;
import org.febit.lang.protocol.IResponse;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface IErrors {

    /**
     * As is the {@code Code}.
     */
    String name();

    int getStatus();

    default ILogger getLogger() {
        return Logger.DEBUG;
    }

    /**
     * Format the given arguments with the given pattern.
     */
    default String format(String code, Object[] args) {
        var msg = I18nUtils.format(code, args);
        this.getLogger().log(this, msg);
        return msg;
    }

    default String format(Exception e, String code, Object[] args) {
        var msg = I18nUtils.format(code, args);
        this.getLogger().log(this, msg, e);
        return msg;
    }

    /**
     * Create a {@link IResponse} that marked as failed.
     *
     * @param pattern message pattern
     * @param args    message args
     */
    @CheckReturnValue
    default <T> IResponse<T> response(String pattern, Object... args) {
        return IResponse.failed(this.getStatus(), this.name(), this.format(pattern, args));
    }

    /**
     * Create a {@link BusinessException}.
     *
     * @param pattern message pattern
     * @param args    message args
     */
    @CheckReturnValue
    default BusinessException exception(String pattern, Object... args) {
        return new BusinessException(this.getStatus(), this.name(), this.format(pattern, args));
    }

    /**
     * Throws a {@link BusinessException}.
     *
     * @param pattern message pattern
     * @param args    message args
     * @see #exception(String, Object...)
     */
    default void failed(String pattern, Object... args) {
        throw this.exception(pattern, args);
    }

    /**
     * Ensures that the specified object reference is not {@code null}.
     *
     * @param obj     the object reference to check for nullity
     * @param pattern message pattern
     * @param args    message args
     * @throws BusinessException if {@code obj} is {@code null}
     */
    default void whenNull(@Nullable Object obj, String pattern, Object... args) {
        if (obj == null) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the specified {@link Collection} is not empty.
     *
     * @param collection the collection to check
     * @param pattern    message pattern
     * @param args       message args
     * @throws BusinessException if {@code collection} is empty
     * @see CollectionUtils#isEmpty(Collection)
     */
    default void whenEmpty(@Nullable Collection<?> collection, String pattern, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the specified {@link CharSequence} is not empty.
     *
     * @param cs      the string to check
     * @param pattern message pattern
     * @param args    message args
     * @throws BusinessException if {@code cs} is empty
     * @see StringUtils#isNotEmpty(CharSequence)
     */
    default void whenEmpty(@Nullable CharSequence cs, String pattern, Object... args) {
        if (StringUtils.isEmpty(cs)) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the specified {@link CharSequence} is not blank.
     *
     * @param cs      the CharSequence to check
     * @param pattern message pattern
     * @param args    message args
     * @throws BusinessException if {@code cs} is blank
     * @see StringUtils#isBlank(CharSequence)
     */
    default void whenBlank(@Nullable CharSequence cs, String pattern, Object... args) {
        if (StringUtils.isBlank(cs)) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the result of specified expression is {@code true}.
     *
     * @param expr    a boolean expression
     * @param pattern message pattern
     * @param args    message args
     * @throws BusinessException if {@code expr} is {@code false}
     */
    default void whenFalse(boolean expr, String pattern, Object... args) {
        if (!expr) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the result is not failed.
     *
     * @param result  the result
     * @param pattern message pattern
     * @param args    message args
     * @throws BusinessException if the result is failed
     */
    default void whenFailed(Fallible result, String pattern, Object... args) {
        if (result.isFailed()) {
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the result is not failed.
     *
     * @param result the result
     * @throws BusinessException if the result is failed
     */
    default void whenFailed(IResponse<?> result) {
        if (result.isFailed()) {
            throw new BusinessException(this.getStatus(), this.name(), result.getMessage());
        }
    }

    /**
     * Ensures that the result is not failed or failed with 404.
     *
     * @param result the result
     * @throws BusinessException if the result is failed
     */
    default void whenFailedExclude404(IResponse<?> result) {
        if (result.isFailed() && result.getStatus() != 404) { // NOPMD
            throw new BusinessException(this.getStatus(), this.name(), result.getMessage());
        }
    }

    /**
     * Ensures that the result is not failed or failed with 404.
     *
     * @param result the result
     * @throws BusinessException if the result is failed
     */
    default void whenFailedExclude404(IResponse<?> result, String pattern, Object... args) {
        if (result.isFailed() && result.getStatus() != 404) { // NOPMD
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the result is not failed with 404.
     *
     * @param result the result
     * @throws BusinessException if the result is failed
     */
    default void when404(IResponse<?> result, String pattern, Object... args) {
        if (result.getStatus() == 404) { // NOPMD
            throw this.exception(pattern, args);
        }
    }

    /**
     * Ensures that the specified {@link Action#apply()} can be run without exceptions.
     *
     * @param action  the action
     * @param pattern message pattern when exception occur
     * @param args    message args
     * @throws BusinessException if throws exceptions when run the action
     */
    default void whenException(Action action, String pattern, Object... args) {
        try {
            action.apply();
        } catch (Exception e) {
            if ((e instanceof ExecutionException)
                    && (e.getCause() instanceof BusinessException)) {
                this.getLogger().log(this, e.getCause().getMessage(), e);
                throw (BusinessException) e.getCause();
            }
            throw new BusinessException(this.getStatus(), this.name(), this.format(e, pattern, args));
        }
    }

    default Object[] normalizeArgs(@Nullable Object[] args) {
        if (args == null) {
            return new Object[0];
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Number) {
                args[i] = args[i].toString();
            }
        }
        return args;
    }

    @RequiredArgsConstructor
    enum Logger implements ILogger {
        OFF(ErrorsLogger::disabled, ErrorsLogger::discard, ErrorsLogger::discard),
        TRACE(ErrorsLogger.LOGGER::isTraceEnabled, msg -> ErrorsLogger.LOGGER.trace("{}", msg), ErrorsLogger.LOGGER::trace),
        DEBUG(ErrorsLogger.LOGGER::isDebugEnabled, msg -> ErrorsLogger.LOGGER.debug("{}", msg), ErrorsLogger.LOGGER::debug),
        INFO(ErrorsLogger.LOGGER::isInfoEnabled, msg -> ErrorsLogger.LOGGER.info("{}", msg), ErrorsLogger.LOGGER::info),
        WARN(ErrorsLogger.LOGGER::isWarnEnabled, msg -> ErrorsLogger.LOGGER.warn("{}", msg), ErrorsLogger.LOGGER::warn),
        ERROR(ErrorsLogger.LOGGER::isErrorEnabled, msg -> ErrorsLogger.LOGGER.error("{}", msg), ErrorsLogger.LOGGER::error),
        ;

        private final BooleanSupplier isEnabled;
        private final Consumer<String> out;
        private final BiConsumer<String, Exception> outWithException;

        private String buildMessage(IErrors err, String msg) {
            return err.name() + '[' + err.getStatus() + "] " + msg;
        }

        @Override
        public boolean isEnabled() {
            return !this.isEnabled.getAsBoolean();
        }

        @Override
        public void log(IErrors err, String msg) {
            if (!this.isEnabled()) {
                return;
            }
            this.out.accept(this.buildMessage(err, msg));
        }

        @Override
        public void log(IErrors err, String msg, Exception ex) {
            if (!this.isEnabled()) {
                return;
            }
            this.outWithException.accept(this.buildMessage(err, msg), ex);
        }
    }

    @FunctionalInterface
    interface Action {
        void apply() throws Exception;
    }

    interface ILogger {
        boolean isEnabled();

        void log(IErrors err, String msg);

        void log(IErrors err, String msg, Exception ex);
    }

}
