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
import org.springframework.beans.FatalBeanException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
class ModelsTest {

    @Test
    void copiesMatchingProperties() {
        var source = new Source();
        source.setName("foo");
        source.setCount("5");

        var target = Models.map(source, Target::new);

        assertThat(target.getName()).isEqualTo("foo");
        assertThat(target.getCount()).isEqualTo(5);
    }

    @Test
    void convertsStringToPrimitiveProperty() {
        var source = new Source();
        source.setCount("42");

        assertThat(Models.map(source, Target::new).getCount()).isEqualTo(42);
    }

    @Test
    void returnsTargetUnchangedWhenSourceNull() {
        var target = new Target();
        target.setName("x");

        var result = Models.map((Object) null, target);

        assertThat(result).isSameAs(target);
        assertThat(result.getName()).isEqualTo("x");
    }

    @Test
    void copiesNullSourceFieldAsNull() {
        var target = new ObjTarget();
        target.setName("preset");
        Models.map(new ObjSource(), target);
        assertThat(target.getName()).isNull();
    }

    @Test
    void throwsWhenValueUnconvertible() {
        var source = new Source();
        source.setCount("not-a-number");
        assertThrows(FatalBeanException.class, () -> Models.map(source, Target::new));
    }

    @Test
    void usesRegisteredCustomConverter() {
        Models.addConverter(From.class, To.class, from -> new To());
        var result = Models.map(new FromHolder(), ToHolder::new);
        assertThat(result.getFrom()).isInstanceOf(To.class);
    }

    @Test
    void registersPlainConverter() {
        Models.addConverter(new Converter<String, To>() {
            @Override
            public To convert(String source) {
                return new To();
            }
        });
        var result = Models.map(new FromStringHolder(), ToHolder::new);
        assertThat(result.getFrom()).isInstanceOf(To.class);
    }

    @Test
    void registersGenericConverter() {
        Models.addConverter(new GenericConverter() {
            @Override
            public Set<ConvertiblePair> getConvertibleTypes() {
                return Set.of(new GenericConverter.ConvertiblePair(String.class, To2.class));
            }

            @Override
            public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
                return new To2();
            }
        });
        var result = Models.map(new FromStringHolder(), To2Holder::new);
        assertThat(result.getFrom()).isInstanceOf(To2.class);
    }

    @Test
    void skipsPropertiesThatCannotBeCopied() {
        var source = new Src();
        source.setName("foo");
        var target = new Tgt();

        Models.map(source, target);

        // matched property is copied
        assertThat(target.getName()).isEqualTo("foo");
        // target property without a matching source property
        assertThat(target.getGhost()).isNull();
        // target property whose source has no getter
        assertThat(target.getWriteOnly()).isNull();
        // target property whose source has no setter
        assertThat(target.getExtra()).isNull();
    }

    @Test
    void setAccessibleIsInvokedForNonPublicBean() {
        var source = new Source();
        source.setName("z");
        var target = new PackagePrivateTarget();

        Models.map(source, target);

        assertThat(target.getName()).isEqualTo("z");
    }

    @Test
    void setAccessibleIsSkippedForPublicBean() {
        var source = new Source();
        source.setName("z");
        var target = new PublicTarget();

        Models.map(source, target);

        assertThat(target.getName()).isEqualTo("z");
    }

    @Getter
    @Setter
    static class Source {
        private String name;
        private String count;
    }

    @Getter
    @Setter
    static class Target {
        private String name;
        private int count;
    }

    @Getter
    @Setter
    static class ObjSource {
        private String name;
    }

    @Getter
    @Setter
    static class ObjTarget {
        private String name;
    }

    static class From {
    }

    static class To {
    }

    @Getter
    @Setter
    static class FromHolder {
        private From from = new From();
    }

    @Getter
    @Setter
    static class ToHolder {
        private To from;
    }

    @Getter
    @Setter
    static class FromStringHolder {
        private String from = "x";
    }

    static class To2 {
    }

    @Getter
    @Setter
    static class To2Holder {
        private To2 from;
    }

    @Setter
    static class Src {
        @Getter
        private String name;
        private String writeOnly;
    }

    @Getter
    static class Tgt {
        @Setter
        private String name;
        @Setter
        private String ghost;
        @Setter
        private String writeOnly;
        private String extra;
    }

    @Setter
    @Getter
    static class PackagePrivateTarget {
        private String name;
    }

    @Setter
    @Getter
    public static class PublicTarget {
        private String name;
    }
}
