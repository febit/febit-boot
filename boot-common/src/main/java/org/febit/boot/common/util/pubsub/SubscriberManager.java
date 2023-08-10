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
package org.febit.boot.common.util.pubsub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.febit.lang.Tuple2;
import org.febit.lang.util.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor(
        staticName = "create",
        access = AccessLevel.PRIVATE
)
public class SubscriberManager<S extends ISubscriber<?>, X> {

    private final ConcurrentMap<Class<?>, List<S>> cachedMapping = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<S>> originMapping;

    private static <S extends ISubscriber<?>> Map<Class<?>, List<S>> mapping(Collection<S> subscribers) {
        return Maps.grouping(
                subscribers.stream()
                        .flatMap(f -> f.subjectTypes().stream()
                                .map(t -> Tuple2.of(t, f))
                        ),
                Tuple2::a, Tuple2::b
        );
    }

    public static <S extends ISubscriber<?>, X> SubscriberManager<S, X> create(Collection<S> subscribers) {
        var mapping = mapping(subscribers);
        return create(mapping);
    }

    private List<S> collectFor(Class<?> type) {
        Set<S> set = SetUtils.newIdentityHashSet();
        for (var entry : this.originMapping.entrySet()) {
            if (!entry.getKey().isAssignableFrom(type)) {
                continue;
            }
            set.addAll(entry.getValue());
        }
        return List.copyOf(set);
    }

    public List<S> resolve(X subject) {
        return resolve(subject.getClass());
    }

    public List<S> resolve(Class<?> type) {
        return this.cachedMapping.computeIfAbsent(type, this::collectFor);
    }

}
