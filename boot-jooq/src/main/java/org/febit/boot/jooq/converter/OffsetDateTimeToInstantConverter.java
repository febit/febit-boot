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

import lombok.RequiredArgsConstructor;
import org.jooq.Converter;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@SuppressWarnings({"unused"})
@RequiredArgsConstructor(staticName = "of")
public class OffsetDateTimeToInstantConverter implements Converter<OffsetDateTime, Instant> {

    private final ZoneOffset zoneOffset;

    public OffsetDateTimeToInstantConverter() {
        this(ZoneOffset.UTC);
    }

    @Nullable
    public Instant from(@Nullable OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        return time.toInstant();
    }

    @Nullable
    @Override
    public OffsetDateTime to(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, zoneOffset);
    }

    @Override
    public Class<OffsetDateTime> fromType() {
        return OffsetDateTime.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
