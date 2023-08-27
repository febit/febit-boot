package org.febit.boot.web.mockmvc.basic.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SampleCategory {
    A(500), B(600);

    @JsonValue
    private final Integer value;
}
