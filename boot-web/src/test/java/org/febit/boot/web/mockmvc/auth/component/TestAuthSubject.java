package org.febit.boot.web.mockmvc.auth.component;

import lombok.Data;
import org.febit.boot.common.auth.AuthSubject;

@Data
public class TestAuthSubject implements AuthSubject {

    private String code;
    private String name;
}
