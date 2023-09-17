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
package org.febit.boot.module;

import org.febit.boot.common.util.Priority;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FebitModuleEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String SOURCE_NAME = "febit-module-environments";

    private static final char DOT = '.';

    private static final String GROUP_ID = "group-id";
    private static final String ARTIFACT_ID = "artifact-id";
    private static final String VERSION = "version";
    private static final String COMMIT_ID = "commit-id";
    private static final String SHORT_COMMIT_ID = "short-commit-id";
    private static final String BUILD_TIME = "build-time";
    private static final String BUILD_TIME_IN_MILLIS = "build-time-in-millis";

    @Override
    public int getOrder() {
        return Priority.HIGH;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var annotations = collectAnnotations(application);
        var props = toProps(annotations);

        if (props.isEmpty()) {
            return;
        }

        environment.getPropertySources().addLast(
                new MapPropertySource(SOURCE_NAME, props)
        );
    }

    static Set<FebitModuleEnvironments> collectAnnotations(SpringApplication application) {
        var mainClass = application.getMainApplicationClass();
        if (mainClass == null) {
            return Set.of();
        }

        var annotations = new HashSet<>(
                AnnotatedElementUtils.findAllMergedAnnotations(mainClass, FebitModuleEnvironments.class)
        );
        var repeat = AnnotatedElementUtils.findMergedAnnotation(mainClass, FebitModuleEnvironments.List.class);
        if (repeat != null) {
            Collections.addAll(annotations, repeat.value());
        }
        return annotations;
    }

    static Map<String, Object> toProps(Set<FebitModuleEnvironments> annotations) {
        if (annotations.isEmpty()) {
            return Map.of();
        }

        var props = new HashMap<String, Object>();
        for (var annotation : annotations) {
            var prefix = annotation.prefix() + DOT;
            var module = BeanUtils.instantiateClass(annotation.value());
            var shortCommitId = module.getCommitId().length() > 8
                    ? module.getCommitId().substring(0, 8)
                    : module.getCommitId();

            props.put(prefix + GROUP_ID, module.getGroupId());
            props.put(prefix + ARTIFACT_ID, module.getArtifactId());
            props.put(prefix + VERSION, module.getVersion());
            props.put(prefix + COMMIT_ID, module.getCommitId());
            props.put(prefix + SHORT_COMMIT_ID, shortCommitId);
            props.put(prefix + BUILD_TIME, module.getBuildTime().toString());
            props.put(prefix + BUILD_TIME_IN_MILLIS,
                    module.getBuildTime().toEpochMilli()
            );
        }
        return props;
    }
}
