/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.filters;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.index.TextIndexer;

/**
 * A helper class to create all type of {@link Filter}s.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class Filters {
    /**
     * A filter to select all elements.
     */
    public static final Filter ALL = null;

    /**
     * Creates an equality filter which matches documents where the value
     * of a field equals the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30
     * collection.find(eq("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the equality filter.
     */
    public static Filter eq(String field, Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates an and filter which performs a logical AND operation on two filters and selects
     * the documents that satisfy both filters.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 and
     * // 'name' field has value as John Doe
     * collection.find(and(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the and filter
     */
    public static Filter and(Filter... filters) {
        return new AndFilter(filters);
    }

    /**
     * Creates an or filter which performs a logical OR operation on two filters and selects
     * the documents that satisfy at least one of the filter.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 or
     * // 'name' field has value as John Doe
     * collection.find(or(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the or filter
     */
    public static Filter or(Filter... filters) {
        return new OrFilter(filters);
    }

    /**
     * Creates a not filter which performs a logical NOT operation on a `filter` and selects
     * the documents that *_do not_* satisfy the `filter`. This also includes documents
     * that do not contain the value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not equals to 30
     * collection.find(not(eq("age", 30)));
     * --
     *
     * @param filter the filter
     * @return the not filter
     */
    public static Filter not(Filter filter) {
        return new NotFilter(filter);
    }

    /**
     * Creates a greater than filter which matches those documents where the value
     * of the value is greater than (i.e. >) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than 30
     * collection.find(gt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater than filter
     */
    public static Filter gt(String field, Object value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those documents where the value
     * of the value is greater than or equals to (i.e. >=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than or equal to 30
     * collection.find(gte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater or equal filter
     */
    public static Filter gte(String field, Object value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those documents where the value
     * of the value is less than (i.e. <) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value less than 30
     * collection.find(lt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser than filter
     */
    public static Filter lt(String field, Object value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those documents where the value
     * of the value is lesser than or equals to (i.e. <=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value lesser than or equal to 30
     * collection.find(lte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser equal filter
     */
    public static Filter lte(String field, Object value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'address' field has value 'roads'.
     * collection.find(text("address", "roads"));
     * --
     *
     * @param field the value
     * @param value the text value
     * @return the text filter
     * @see TextIndexer
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    public static Filter text(String field, String value) {
        return new TextFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in documents.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'name' value starts with 'jim' or 'joe'.
     * collection.find(regex("name", "^(jim|joe).*"));
     * --
     *
     * @param field the value
     * @param value the regular expression
     * @return the regex filter
     */
    public static Filter regex(String field, String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates an in filter which matches the documents where
     * the value of a field equals any value in the specified array.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value in [20, 30, 40]
     * collection.find(in("age", 20, 30, 40));
     * --
     *
     * @param field  the value
     * @param values the range values
     * @return the in filter
     */
    public static Filter in(String field, Object... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates an element match filter that matches documents that contain an array
     * value with at least one element that matches the specified `filter`.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents which has an array field - 'color' and the array
     * // contains a value - 'red'.
     * collection.find(elemMatch("color", eq("$", "red"));
     * --
     *
     * @param field  the value
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    public static Filter elemMatch(String field, Filter filter) {
        return new ElementMatchFilter(field, filter);
    }
}
