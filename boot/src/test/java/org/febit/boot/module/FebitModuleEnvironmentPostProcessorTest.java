package org.febit.boot.module;

import org.febit.boot.FebitBoot;
import org.febit.boot.FebitBootCommon;
import org.febit.boot.common.util.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.boot.module.FebitModuleEnvironmentPostProcessor.SOURCE_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FebitModuleEnvironmentPostProcessorTest {

    @Test
    void getOrder() {
        assertEquals(Priority.HIGH, new FebitModuleEnvironmentPostProcessor().getOrder());
    }

    @Test
    void postProcessEnvironment() {
        var processor = new FebitModuleEnvironmentPostProcessor();
        var app = mock(SpringApplication.class);
        var env = new MockEnvironment();
        var originalSize = env.getPropertySources().size();

        when(app.getMainApplicationClass()).thenReturn(null);
        processor.postProcessEnvironment(env, app);
        assertEquals(originalSize, env.getPropertySources().size());

        when(app.getMainApplicationClass()).then(inv -> AppSingleAnnotation.class);
        processor.postProcessEnvironment(env, app);
        assertEquals(originalSize + 1, env.getPropertySources().size());
        assertThat(env.getPropertySources().get(SOURCE_NAME))
                .isNotNull()
                .returns(FebitBootCommon.groupId(), s -> s.getProperty("app.module.group-id"))
        ;
    }

    @Test
    void collectAnnotations() {
        var app = mock(SpringApplication.class);

        when(app.getMainApplicationClass()).thenReturn(null);
        assertEquals(Set.of(), FebitModuleEnvironmentPostProcessor.collectAnnotations(app));

        when(app.getMainApplicationClass()).then(inv -> AppNoAnnotation.class);
        assertEquals(Set.of(), FebitModuleEnvironmentPostProcessor.collectAnnotations(app));

        when(app.getMainApplicationClass()).then(inv -> AppSingleAnnotation.class);
        assertEquals(Set.of(
                AppSingleAnnotation.class.getAnnotation(FebitModuleEnvironments.class)
        ), FebitModuleEnvironmentPostProcessor.collectAnnotations(app));

        when(app.getMainApplicationClass()).then(inv -> AppMultiAnnotation.class);
        assertEquals(Set.of(
                AppMultiAnnotation.class.getAnnotation(FebitModuleEnvironments.List.class).value()
        ), FebitModuleEnvironmentPostProcessor.collectAnnotations(app));
    }

    @Test
    void toProps() {
        assertEquals(Map.of(), FebitModuleEnvironmentPostProcessor.toProps(Set.of()));

        assertThat(FebitModuleEnvironmentPostProcessor.toProps(Set.of(
                AppMultiAnnotation.class.getAnnotation(FebitModuleEnvironments.List.class).value()
        )))
                .containsEntry("febit-boot-common.group-id", FebitBootCommon.groupId())
                .containsEntry("febit-boot-common.artifact-id", FebitBootCommon.artifactId())
                .containsEntry("febit-boot-common.version", FebitBootCommon.version())
                .containsEntry("febit-boot-common.commit-id", FebitBootCommon.commitId())
                .containsEntry("febit-boot-common.short-commit-id", FebitBootCommon.commitId().substring(0, 8))
                .containsEntry("febit-boot-common.built-at", FebitBootCommon.builtAt().toString())
                .containsEntry("febit-boot-common.built-at-millis",
                        FebitBootCommon.builtAt().toEpochMilli()
                )
                .containsEntry("febit-boot.group-id", FebitBoot.groupId())
                .containsEntry("febit-boot.artifact-id", FebitBoot.artifactId())
                .containsEntry("febit-boot.version", FebitBoot.version())
                .containsEntry("febit-boot.commit-id", FebitBoot.commitId())
                .containsEntry("febit-boot.short-commit-id", FebitBoot.commitId().substring(0, 8))
                .containsEntry("febit-boot.built-at", FebitBoot.builtAt().toString())
                .containsEntry("febit-boot.built-at-millis",
                        FebitBoot.builtAt().toEpochMilli()
                );
    }

    static class AppNoAnnotation {
    }

    @FebitModuleEnvironments(FebitBootCommon.class)
    static class AppSingleAnnotation {
    }

    @FebitModuleEnvironments(value = FebitBootCommon.class, prefix = "febit-boot-common")
    @FebitModuleEnvironments(value = FebitBoot.class, prefix = "febit-boot")
    static class AppMultiAnnotation {
    }
}
