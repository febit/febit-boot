package org.febit.boot.web.mockmvc.basic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SampleVO {

    private Long id;
    private SampleStatus status;
    private SampleCategory category;
    private SampleScope scope;
}
