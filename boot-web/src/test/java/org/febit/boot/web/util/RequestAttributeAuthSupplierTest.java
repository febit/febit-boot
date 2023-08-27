package org.febit.boot.web.util;

import org.febit.boot.common.auth.AuthConstants;
import org.febit.boot.common.exception.BusinessException;
import org.febit.boot.common.util.Errors;
import org.febit.boot.web.mockmvc.auth.component.TestAuthSubject;
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

        var auth = new TestAuthSubject();
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
