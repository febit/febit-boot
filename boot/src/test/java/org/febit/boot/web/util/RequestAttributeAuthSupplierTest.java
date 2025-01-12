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
package org.febit.boot.web.util;

import org.febit.boot.auth.AuthConstants;
import org.febit.boot.auth.web.RequestAttributeAuthSupplier;
import org.febit.boot.common.util.Errors;
import org.febit.boot.web.mockmvc.auth.component.TestAuthSubject;
import org.febit.lang.protocol.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestAttributeAuthSupplierTest {

    @Test
    void get() {
        var supplier = RequestAttributeAuthSupplier.create(TestAuthSubject.class);
        var attrs = mock(RequestAttributes.class);

        assertTrue(supplier.get().isEmpty());
        RequestContextHolder.setRequestAttributes(attrs);
        assertTrue(supplier.get().isEmpty());

        var auth = new TestAuthSubject(null, null);
        when(attrs.getAttribute(AuthConstants.ATTR_AUTH, RequestAttributes.SCOPE_REQUEST))
                .thenReturn(auth);
        assertSame(auth, supplier.get().get());

        when(attrs.getAttribute(AuthConstants.ATTR_AUTH, RequestAttributes.SCOPE_REQUEST))
                .thenReturn(new Object());

        var ex = assertThrows(BusinessException.class, supplier::get);
        assertEquals(Errors.SYSTEM.name(), ex.getCode());
        assertEquals(Errors.SYSTEM.getStatus(), ex.getStatus());
    }
}
