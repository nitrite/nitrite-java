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

import org.dizitart.no2.index.TextIndexer;

/**
 * @author Anindya Chatterjee.
 */
public final class FluentFilter {
    public static FluentFilter $ = where("$");
    private String field;

    private FluentFilter() {
    }

    public static FluentFilter where(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates an equality filter which matches documents where the value
     * of a field equals the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30
     * collection.find(where("age").eq(30));
     * --
     *
     * @param value the value
     * @return the equality filter.
     */
    public Filter eq(Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates a greater than filter which matches those documents where the value
     * of the field is greater than (i.e. >) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than 30
     * collection.find(where("age").gt(30));
     * --
     *
     * @param value the value
     * @return the greater than filter
     */
    public Filter gt(Comparable<?> value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those documents where the value
     * of the field is greater than or equals to (i.e. >=) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than or equal to 30
     * collection.find(where("age").gte(30));
     * --
     *
     * @param value the value
     * @return the greater or equal filter
     */
    public Filter gte(Comparable<?> value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those documents where the value
     * of the field is less than (i.e. <) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value less than 30
     * collection.find(where("age").lt(30));
     * --
     *
     * @param value the value
     * @return the lesser than filter
     */
    public Filter lt(Comparable<?> value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those documents where the value
     * of the field is lesser than or equals to (i.e. <=) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value lesser than or equal to 30
     * collection.find(where("age").lte(30));
     * --
     *
     * @param value the value
     * @return the lesser equal filter
     */
    public Filter lte(Comparable<?> value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound including the end values.
     * <p>
     * <pre> {@code
     * // matches all documents where 'age' field is between 30 and 40
     * collection.find(where("age").between(40, 30));
     * }
     * </pre>
     *
     * @param lowerBound the lower value
     * @param upperBound the upper value
     * @return the between filter
     */
    public Filter between(Comparable<?> lowerBound, Comparable<?> upperBound) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound));
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound.
     * <p>
     * <pre> {@code
     * // matches all documents where 'age' field is
     * // between 30 and 40, excluding 30 and 40
     * collection.find(where("age").between(40, 30, false));
     * }
     * </pre>
     *
     * @param lowerBound the lower value
     * @param upperBound the upper value
     * @param inclusive indicates whether to include end values
     * @return the between filter
     */
    public Filter between(Comparable<?> lowerBound, Comparable<?> upperBound, boolean inclusive) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound, inclusive));
    }

    /**
     * Creates a between filter which matches those documents where the value
     * of the field is within the specified bound.
     * <p>
     * <pre> {@code
     * // matches all documents where 'age' field is
     * // between 30 and 40, including 40 and excluding 30
     * collection.find(where("age").between(40, 30, true, false));
     * }
     * </pre>
     *
     * @param lowerBound the lower value
     * @param upperBound the upper value
     * @param lowerInclusive indicates whether to include lower end value
     * @param upperInclusive indicates whether to include upper end value
     * @return the between filter
     */
    public Filter between(Comparable<?> lowerBound, Comparable<?> upperBound, boolean lowerInclusive, boolean upperInclusive) {
        return new BetweenFilter<>(field, new BetweenFilter.Bound<>(lowerBound, upperBound, lowerInclusive, upperInclusive
        ));
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'address' field has value 'roads'.
     * collection.find(where("address").text("roads"));
     * --
     *
     * @param value the text value
     * @return the text filter
     * @see TextIndexer
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    public Filter text(String value) {
        return new TextFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in documents.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'name' value starts with 'jim' or 'joe'.
     * collection.find(where("address").regex("^(jim|joe).*"));
     * --
     *
     * @param value the regular expression
     * @return the regex filter
     */
    public Filter regex(String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates an in filter which matches the documents where
     * the value of a field equals any value in the specified array.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value in [20, 30, 40]
     * collection.find(where("age").in(20, 30, 40));
     * --
     *
     * @param values the range values
     * @return the in filter
     */
    public Filter in(Comparable<?>... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates a notIn filter which matches the documents where
     * the value of a field not equals any value in the specified array.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not in [20, 30, 40]
     * collection.find(where("age").notIn(20, 30, 40));
     * --
     *
     * @param values the range values
     * @return the notIn filter
     */
    public Filter notIn(Comparable<?>... values) {
        return new NotInFilter(field, values);
    }

    /**
     * Creates an element match filter that matches documents that contain an array
     * value with at least one element that matches the specified `filter`.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents which has an array field - 'color' and the array
     * // contains a value - 'red'.
     * collection.find(where("age").elemMatch($.eq("red")));
     * --
     *
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    public Filter elemMatch(Filter filter) {
        return new ElementMatchFilter(field, filter);
    }
}
