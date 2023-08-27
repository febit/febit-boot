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
package org.febit.boot.web.component;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.boot.common.util.Errors;
import org.febit.boot.common.util.Priority;
import org.febit.lang.protocol.Pagination;
import org.febit.lang.protocol.Sort;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Order(Priority.HIGHER)
public class PaginationArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_SORT = "sort";
    private static final String PARAM_ORDER = "order";

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 2000;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    static int getIntParameter(WebRequest request, String name, int defaultValue) {
        var raw = request.getParameter(name);
        if (StringUtils.isBlank(raw)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw Errors.ILLEGAL_ARG.exception(
                    "A number is required for parameter ''{0}''", name);
        }
    }

    static List<Sort> parseOrders(@Nullable String[] raws, Sort.Direction defaultDirection) {
        if (raws == null || raws.length == 0) {
            return List.of();
        }

        return Stream.of(raws)
                .flatMap(sort -> sort == null
                        ? Stream.empty()
                        : Stream.of(StringUtils.split(sort, '|'))
                )
                .map(raw -> parseOrder(raw, defaultDirection))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    static Sort parseOrder(String raw, Sort.Direction defaultDirection) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        raw = raw.trim();
        var spit = raw.lastIndexOf(',');

        var direction = parseDirection(
                spit < 0 ? null : raw.substring(spit + 1).trim(),
                defaultDirection
        );

        var column = spit < 0 ? raw
                : raw.substring(0, spit).trim();

        return Sort.of(column, direction);
    }

    static Sort.Direction parseDirection(@Nullable String raw, Sort.Direction defaultDirect) {
        if (StringUtils.isBlank(raw)) {
            return defaultDirect;
        }

        return switch (raw.trim().toLowerCase()) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw Errors.ILLEGAL_ARG.exception(
                    "Only ''asc'' and ''desc'' directions are allow for parameter ''{0}''",
                    PARAM_SORT
            );
        };
    }

    @Override
    public boolean supportsParameter(MethodParameter param) {
        var paramType = param.getParameterType();
        if (paramType.equals(Pagination.class)) {
            return true;
        }
        if (Pagination.class.isAssignableFrom(paramType)) {
            log.error("You are using a Pagination implements type [{}], in method [{}],"
                            + " which we can't handle, please use Pagination directly!",
                    paramType, param.getMethod()
            );
        }
        return false;
    }

    @Override
    public Pagination resolveArgument(
            MethodParameter methodParam,
            @Nullable ModelAndViewContainer modelAndView,
            NativeWebRequest req,
            @Nullable WebDataBinderFactory binderFactory
    ) {
        var page = getIntParameter(req, PARAM_PAGE, DEFAULT_PAGE);
        var size = getIntParameter(req, PARAM_SIZE, DEFAULT_SIZE);
        var defaultDirection = parseDirection(
                req.getParameter(PARAM_ORDER),
                DEFAULT_DIRECTION
        );

        var ordersRaw = req.getParameterValues(PARAM_SORT);
        var orders = parseOrders(ordersRaw, defaultDirection);

        var pagination = new Pagination();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setSorts(orders);
        return pagination;
    }
}
