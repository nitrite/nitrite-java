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
import org.dizitart.no2.collection.objects.ObjectFilter;
import org.dizitart.no2.index.TextIndexer;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * A helper class to create all type of {@link ObjectFilter}s.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class ObjectFilters {
    /**
     * A filter to select all elements.
     */
    public static final ObjectFilter ALL = null;

    /**
     * Creates an and filter which performs a logical AND operation on two filters and selects
     * the objects that satisfy both filters.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value as 30 and
     * // 'name' field has value as John Doe
     * repository.find(and(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the and filter
     */
    public static ObjectFilter and(ObjectFilter... filters) {
        notNull(filters, errorMessage("filters can not be null", VE_OBJ_FILTER_NULL_AND_FILTERS));
        return new AndObjectFilter(filters);
    }

    /**
     * Creates an or filter which performs a logical OR operation on two filters and selects
     * the objects that satisfy at least one of the filter.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value as 30 or
     * // 'name' field has value as John Doe
     * repository.find(or(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the or filter
     */
    public static ObjectFilter or(ObjectFilter... filters) {
        notNull(filters, errorMessage("filters can not be null", VE_OBJ_FILTER_NULL_OR_FILTERS));
        return new OrObjectFilter(filters);
    }

    /**
     * Creates a not filter which performs a logical NOT operation on a `filter` and selects
     * the objects that *_do not_* satisfy the `filter`. This also includes objects
     * that do not contain the value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value not equals to 30
     * repository.find(not(eq("age", 30)));
     * --
     *
     * @param filter the filter
     * @return the not filter
     */
    public static ObjectFilter not(ObjectFilter filter) {
        notNull(filter, errorMessage("filter can not be null", VE_OBJ_FILTER_NULL_NOT_FILTERS));
        return new NotObjectFilter(filter);
    }

    /**
     * Creates an equality filter which matches objects where the value
     * of a field equals the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value as 30
     * repository.find(eq("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the equality filter.
     */
    public static ObjectFilter eq(String field, Object value) {
        return new EqualsObjectFilter(field, value);
    }

    /**
     * Creates a greater than filter which matches those objects where the value
     * of the field is greater than (i.e. >) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value greater than 30
     * repository.find(gt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater than filter
     */
    public static ObjectFilter gt(String field, Object value) {
        return new GreaterObjectFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those objects where the value
     * of the field is greater than or equals to (i.e. >=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value greater than or equal to 30
     * repository.find(gte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater or equal filter
     */
    public static ObjectFilter gte(String field, Object value) {
        return new GreaterEqualObjectFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those objects where the value
     * of the field is less than (i.e. <) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value less than 30
     * repository.find(lt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser than filter
     */
    public static ObjectFilter lt(String field, Object value) {
        return new LessThanObjectFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those objects where the value
     * of the field is lesser than or equals to (i.e. <=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value lesser than or equal to 30
     * repository.find(lte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser equal filter
     */
    public static ObjectFilter lte(String field, Object value) {
        return new LesserEqualObjectFilter(field, value);
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'address' field has value 'roads'.
     * repository.find(text("address", "roads"));
     * --
     *
     * @param field the value
     * @param value the text value
     * @return the text filter
     * @see TextIndexer
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    public static ObjectFilter text(String field, String value) {
        return new TextObjectFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in objects.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'name' value starts with 'jim' or 'joe'.
     * repository.find(regex("name", "^(jim|joe).*"));
     * --
     *
     * @param field the value
     * @param value the regular expression
     * @return the regex filter
     */
    public static ObjectFilter regex(String field, String value) {
        return new RegexObjectFilter(field, value);
    }

    /**
     * Creates an in filter which matches the objects where
     * the value of a field equals any value in the specified array.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects where 'age' field has value in [20, 30, 40]
     * repository.find(in("age", 20, 30, 40));
     * --
     *
     * @param field  the value
     * @param values the range values
     * @return the in filter
     */
    public static ObjectFilter in(String field, Object... values) {
        return new InObjectFilter(field, values);
    }

    /**
     * Creates an element match filter that matches objects that contain an array
     * value with at least one element that matches the specified `filter`.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all objects which has an array field - 'color' and the array
     * // contains a value - 'red'.
     * repository.find(elemMatch("color", eq("$", "red"));
     * --
     *
     * @param field  the value
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    public static ObjectFilter elemMatch(final String field, final ObjectFilter filter) {
        return new ElemMatchObjectFilter(field, filter);
    }
}
