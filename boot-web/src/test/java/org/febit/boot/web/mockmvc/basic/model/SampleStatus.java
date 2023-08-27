package org.febit.boot.web.mockmvc.basic.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.lang.Valued;

@Getter
@RequiredArgsConstructor
public enum SampleStatus implements Valued<Integer> {
    OK(100), FAILED(200);
    private final Integer value;
}
