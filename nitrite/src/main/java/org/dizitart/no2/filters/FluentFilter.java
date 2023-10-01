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
 * A fluent api for the {@link NitriteFilter}.
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
     * Creates a new {@link FluentFilter} instance with the specified field name.
     *
     * @param field the name of the field to filter on
     * @return a new {@link FluentFilter} instance with the specified field name
     */
    public static FluentFilter where(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates an equality filter that matches documents where the value of a 
     * field equals the specified value.
     *
     * @param value the value to match against.
     * @return a {@link NitriteFilter} instance representing the equality filter.
     */
    public NitriteFilter eq(Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates a filter that matches all documents where the value of 
     * the specified field is not equal to the given value.
     *
     * @param value the value to compare against.
     * @return a {@link NitriteFilter} instance.
     */
    public NitriteFilter notEq(Object value) {
        return new NotEqualsFilter(field, value);
    }

    /**
     * Creates a filter that matches all documents where the value of 
     * the specified field is greater than the given value.
     *
     * @param value the value to compare against.
     * @return the NitriteFilter instance representing the greater than filter.
     */
    public NitriteFilter gt(Comparable<?> value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a filter that matches documents where the value of the 
     * field is greater than or equal to the specified value.
     *
     * @param value the value to compare against.
     * @return a filter that matches documents where the value of the field is greater than or equal to the specified value.
     */
    public NitriteFilter gte(Comparable<?> value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a filter that matches documents where the value of the 
     * field is less than the specified value.
     *
     * @param value the value to compare against.
     * @return a filter that matches documents where the value of the field is less than the specified value.
     */
    public NitriteFilter lt(Comparable<?> value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a filter that matches documents where the value of the 
     * field is less than or equal to the specified value.
     *
     * @param value the value to compare against.
     * @return a filter that matches documents where the value of the field is less than or equal to the specified value.
     */
    public NitriteFilter lte(Comparable<?> value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a filter that matches documents where the value of the field is 
     * between the specified lower and upper bounds.
     *
     * @param lowerBound the lower bound (inclusive) of the range to match.
     * @param upperBound the upper bound (inclusive) of the range to match.
     * @return a filter that matches documents where the value of the field is 
     * between the specified lower and upper bounds.
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound));
    }

    /**
     * Creates a filter that matches documents where the value of the field is 
     * between the given lower and upper bounds.
     *
     * @param lowerBound the lower bound of the range (inclusive).
     * @param upperBound the upper bound of the range (inclusive).
     * @param inclusive  whether the bounds are inclusive or not.
     * @return a filter that matches documents where the value of the field is 
     * between the given lower and upper bounds.
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound, boolean inclusive) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound, inclusive));
    }


    /**
     * Creates a filter that matches documents where the value of a field is 
     * between two specified values.
     *
     * @param lowerBound the lower bound (inclusive) of the range
     * @param upperBound the upper bound (inclusive) of the range
     * @param lowerInclusive true if the lower bound is inclusive, false otherwise
     * @param upperInclusive true if the upper bound is inclusive, false otherwise
     * @return a filter that matches documents where the value of a field is 
     * between two specified values
     */
    public NitriteFilter between(Comparable<?> lowerBound, Comparable<?> upperBound,
                          boolean lowerInclusive, boolean upperInclusive) {
        return new BetweenFilter<>(field,
            new BetweenFilter.Bound<>(lowerBound, upperBound, lowerInclusive, upperInclusive
        ));
    }

    /**
     * Creates a filter which performs a text search on the content of the fields
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
     * Creates a filter that matches documents where the value of the specified field matches the
     * specified regular expression pattern.
     *
     * @param value the regular expression pattern to match against the value of the specified field
     * @return a filter that matches documents where the value of the specified field 
     * matches the specified regular expression pattern
     */
    public NitriteFilter regex(String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates a filter that matches documents where the value of the field is in the specified array of values.
     *
     * @param values the array of values to match against
     * @return the filter object representing the filter
     */
    public NitriteFilter in(Comparable<?>... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates a filter that matches documents where the value of the field is not in the specified array of values.
     *
     * @param values the array of values to compare against
     * @return a filter that matches documents where the value of the field is not in the specified array of values
     */
    public NitriteFilter notIn(Comparable<?>... values) {
        return new NotInFilter(field, values);
    }

    /**
     * Creates a filter that matches documents where the value of a field contains at least one element that matches the
     * specified filter.
     *
     * @param filter the filter to apply to the matching elements
     * @return a filter that matches documents where the value of a field contains at least one element that matches the
     * specified filter
     */
    public NitriteFilter elemMatch(Filter filter) {
        return new ElementMatchFilter(field, filter);
    }
}
