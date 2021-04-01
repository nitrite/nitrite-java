/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.filters;

/**
 * A fluent api for the {@link Filter}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public final class FluentFilter {
    /**
     * The where clause for elemMatch filter.
     */
    public static FluentFilter $ = where("$");

    private String field;

    private FluentFilter() {
    }

    /**
     * Where clause for fluent filter.
     *
     * @param field the field
     * @return the fluent filter
     */
    public static FluentFilter where(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates an equality filter which matches documents where the value
     * of a field equals the specified value.
     *
     *
     * @param value the value
     * @return the equality filter.
     */
    public NitriteFilter eq(Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates an equality filter which matches documents where the value
     * of a field not equals the specified value.
     *
     * @param value the value
     * @return the filter
     */
    public NitriteFilter notEq(Object value) {
        return new NotEqualsFilter(field, value);
    }

    /**
     * Creates a greater than filter which matches those documents where the value
     * of the field is greater than (i.e. &gt;) the specified value.
     *
     * @param value the value
     * @return the greater than filter
     */
    public NitriteFilter gt(Comparable<?> value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those documents where the value
     * of the field is greater than or equals to (i.e. &ge;) the specified value.
     *
     * @param value the value
     * @return the greater or equal filter
     */
    public NitriteFilter gte(Comparable<?> value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those documents where the value
     * of the field is less than (i.e. &lt;) the specified value.
     *
     * @param value the value
     * @return the lesser than filter
     */
    public NitriteFilter lt(Comparable<?> value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those documents where the value
     * of the field is lesser than or equals to (i.e. &le;) the specified value.
     *
     * @param value the value
     * @return the lesser equal filter
     */
    public NitriteFilter lte(Comparable<?> value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound including the end values.
     * <pre> {@code
     * // matches all documents where 'age' field is between 30 and 40
     * collection.find(where("age").between(40, 30));
     * }*
     * </pre>
     *
     * @param lowerBound the lower value
     * @param upperBound the upper value
     * @return the between filter
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound));
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound.
     * <pre> {@code
     * // matches all documents where 'age' field is
     * // between 30 and 40, excluding 30 and 40
     * collection.find(where("age").between(40, 30, false));
     * }*
     * </pre>
     *
     * @param lowerBound the lower value
     * @param upperBound the upper value
     * @param inclusive  indicates whether to include end values
     * @return the between filter
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound, boolean inclusive) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound, inclusive));
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound.
     * <pre> {@code
     * // matches all documents where 'age' field is
     * // between 30 and 40, including 40 and excluding 30
     * collection.find(where("age").between(40, 30, true, false));
     * }*
     * </pre>
     *
     * @param lowerBound     the lower value
     * @param upperBound     the upper value
     * @param lowerInclusive indicates whether to include lower end value
     * @param upperInclusive indicates whether to include upper end value
     * @return the between filter
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound,
                          boolean lowerInclusive, boolean upperInclusive) {
        return new BetweenFilter<>(field,
            new BetweenFilter.Bound<>(lowerBound, upperBound, lowerInclusive, upperInclusive
        ));
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     *
     * @param value the text value
     * @return the text filter
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    public NitriteFilter text(String value) {
        return new TextFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in documents.
     *
     * @param value the regular expression
     * @return the regex filter
     */
    public NitriteFilter regex(String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates an in filter which matches the documents where
     * the value of a field equals any value in the specified array.
     *
     * @param values the range values
     * @return the in filter
     */
    public NitriteFilter in(Comparable<?>... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates a notIn filter which matches the documents where
     * the value of a field not equals any value in the specified array.
     *
     * @param values the range values
     * @return the notIn filter
     */
    public NitriteFilter notIn(Comparable<?>... values) {
        return new NotInFilter(field, values);
    }

    /**
     * Creates an element match filter that matches documents that contain an array
     * value with at least one element that matches the specified filter.
     *
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    public NitriteFilter elemMatch(Filter filter) {
        return new ElementMatchFilter(field, filter);
    }
}
