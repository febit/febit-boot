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
package org.febit.boot.web.mockmvc.basic.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.boot.web.mockmvc.basic.model.SampleForm;
import org.febit.boot.web.mockmvc.basic.model.SampleScope;
import org.febit.boot.web.mockmvc.basic.model.SampleSearchForm;
import org.febit.boot.web.mockmvc.basic.model.SampleStatus;
import org.febit.boot.web.mockmvc.basic.model.SampleVO;
import org.febit.lang.protocol.IBasicApi;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.protocol.Page;
import org.febit.lang.protocol.Pagination;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Sample API")
@RequestMapping(value = {
        "/api/v1/samples"
}, produces = {
        MediaType.APPLICATION_JSON_VALUE
})
public class SampleApi implements IBasicApi {

    @GetMapping("/{id}")
    public IResponse<SampleVO> requireById(
            @PathVariable Long id
    ) {
        return ok(
                data()
        );
    }

    @PostMapping("/list")
    public IResponse<Collection<SampleVO>> list(
            @RequestBody @Valid SampleSearchForm form
    ) {
        return ok(
                List.of(data())
        );
    }

    @PostMapping("/search")
    public IResponse<Page<SampleVO>> search(
            Pagination page,
            @RequestBody @Valid SampleSearchForm form
    ) {
        return ok(
                Page.of(page, 100, List.of(data()))
        );
    }

    @PostMapping
    public IResponse<SampleVO> create(
            @RequestBody @Valid SampleForm form
    ) {
        return ok(data());
    }

    @PutMapping("/{id}")
    public IResponse<SampleVO> update(
            @PathVariable Long id,
            @RequestBody @Valid SampleForm form
    ) {
        return ok(data());
    }

    @DeleteMapping("/{id}")
    public IResponse<Void> deleteById(
            @PathVariable Long id
    ) {
        return ok();
    }

    @DeleteMapping("/by-ids/{ids}")
    public IResponse<Void> deleteByIds(
            @PathVariable List<Long> ids
    ) {
        return ok();
    }

    private SampleVO data() {
        return SampleVO.builder()
                .id(1L)
                .status(SampleStatus.FAILED)
                .scope(SampleScope.SESSION)
                .build();
    }

}
