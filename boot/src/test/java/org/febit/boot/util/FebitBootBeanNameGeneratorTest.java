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
package org.febit.boot.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FebitBootBeanNameGeneratorTest {

    private final FebitBootBeanNameGenerator generator = new FebitBootBeanNameGenerator();
    private final BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

    private String generate(String className) {
        var definition = new RootBeanDefinition();
        definition.setBeanClassName(className);
        return generator.generateBeanName(definition, registry);
    }

    @TableTest("""
        scenario                  | input                      | expected
        rewrite prefix            | org.febit.boot.MyService   | febit.MyService
        keep non-prefix           | com.example.OtherService   | com.example.OtherService
        exact prefix no class     | org.febit.boot             | org.febit.boot
        no false substring match  | org.febit.bootxyz.Foo      | org.febit.bootxyz.Foo
        nested package            | org.febit.boot.inner.X     | febit.inner.X
        """)
    void generatesExpectedName(String input, String expected) {
        assertThat(generate(input)).isEqualTo(expected);
    }

    @Test
    void nullBeanClassNameThrows() {
        var definition = new RootBeanDefinition();
        definition.setBeanClassName(null);
        assertThatThrownBy(() -> generator.generateBeanName(definition, registry))
                .isInstanceOf(IllegalStateException.class);
    }
}
