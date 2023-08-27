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
package org.febit.boot.jooq.converter;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jooq.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SuppressWarnings({"unused"})
@RequiredArgsConstructor(staticName = "of")
public class LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {

    private final ZoneOffset zoneOffset;

    public LocalDateTimeToInstantConverter() {
        this(ZoneOffset.UTC);
    }

    @Nullable
    @Override
    public Instant from(@Nullable LocalDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.atOffset(zoneOffset).toInstant();
    }

    @Nullable
    @Override
    public LocalDateTime to(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.from(instant.atOffset(zoneOffset));
    }

    @Override
    public Class<LocalDateTime> fromType() {
        return LocalDateTime.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
