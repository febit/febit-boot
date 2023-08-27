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
package org.febit.boot.feign;

import feign.InvocationHandlerFactory;
import feign.Target;
import feign.codec.Decoder;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings({
        "unused",
        "squid:S00112", // Generic exceptions should never be thrown
        "squid:S1172", // Unused method parameters should be removed
})
public class StandardInvocationHandler implements InvocationHandler {

    protected final Target<?> target;
    protected final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;
    protected final Decoder decoder;

    public static InvocationHandlerFactory factory(Decoder decoder) {
        return (target, dispatch) -> new StandardInvocationHandler(target, dispatch, decoder);
    }

    @Nullable
    @Override
    public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        return switch (method.getName()) {
            case "equals" -> invokeEquals(args);
            case "hashCode" -> hashCode();
            case "toString" -> toString();
            default -> invoke(method, args);
        };
    }

    @Nullable
    protected Object invoke(Method method, @Nullable Object[] args) throws Throwable {
        var handler = this.dispatch.get(method);
        try {
            FeignApiArgs.HOLDER.set(args != null ? args : ArrayUtils.EMPTY_OBJECT_ARRAY);
            return handler.invoke(args);
        } catch (Exception ex) {
            FeignApiArgs.HOLDER.remove();
            return handleGenericException(method, args, ex);
        } finally {
            FeignApiArgs.HOLDER.remove();
        }
    }

    private boolean invokeEquals(@Nullable Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return false;
        }
        return equals(Proxy.getInvocationHandler(args[0]));
    }

    @Nullable
    protected Object handleGenericException(Method method, @Nullable Object[] args, Exception ex) throws Exception {
        if (ex instanceof ResponseErrorException) {
            return handleResponseErrorException(method, args, (ResponseErrorException) ex);
        }
        throw ex;
    }

    @Nullable
    protected Object handleResponseErrorException(Method method, @Nullable Object[] args, ResponseErrorException ex)
            throws Exception {
        var type = method.getGenericReturnType();
        if (type == void.class) {
            // NOTE: Ignore response
            return null;
        }
        return this.decoder.decode(ex.getSnapshot().toResponse(), type);
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StandardInvocationHandler)) {
            return false;
        }
        return this.target.equals(((StandardInvocationHandler) obj).target);
    }

    @Override
    public String toString() {
        return this.target.toString();
    }
}
