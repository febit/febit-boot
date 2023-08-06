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
package org.febit.boot.jooq;

import lombok.val;
import org.febit.lang.annotation.NonNullArgs;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.core.annotation.AliasFor;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface SearchForm {

    @Nonnull
    default List<Condition> toConditions(DSLContext dsl) {
        val conditions = new ArrayList<Condition>();
        SearchFormUtils.collectAnnotatedConditions(dsl, this, conditions::add);
        apply(dsl, conditions::add);
        return conditions;
    }

    /**
     * Override to push custom conditions.
     */
    @NonNullArgs
    default void apply(DSLContext dsl, Consumer<Condition> consumer) {
    }

    @Column(operator = Column.Operator.KEYWORD)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Keyword {

        @AliasFor(annotation = Column.class, attribute = "values")
        String[] value() default {};

        @AliasFor(annotation = Column.class, attribute = "names")
        Column.Name[] names() default {};

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.NOT_CONTAINS)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotContains {
        @AliasFor(annotation = Column.class)
        String value() default "";

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.CONTAINS)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Contains {
        @AliasFor(annotation = Column.class)
        String value() default "";

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.STARTS_WITH)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface StartsWith {
        @AliasFor(annotation = Column.class)
        String value() default "";

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.ENDS_WITH)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface EndsWith {
        @AliasFor(annotation = Column.class)
        String value() default "";

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.EQ)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Equals {
        @AliasFor(annotation = Column.class)
        String value() default "";

        @AliasFor(annotation = Column.class, attribute = "ignoreCase")
        boolean ignoreCase() default false;
    }

    @Column(operator = Column.Operator.GT)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface GreaterThan {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }

    @Column(operator = Column.Operator.GE)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface GreaterEquals {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }

    @Column(operator = Column.Operator.LT)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface LessThan {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }

    @Column(operator = Column.Operator.LE)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface LessEquals {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }

    @Column(operator = Column.Operator.IN)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface In {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }

    @Column(operator = Column.Operator.NOT_IN)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotIn {
        @AliasFor(annotation = Column.class)
        String value() default "";
    }
}
