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

package org.dizitart.no2.collection;

import lombok.Getter;
import lombok.experimental.NonFinal;

/**
 * Represents the options to specify during a find operation on a collection.
 *
 * [[app-listing]]
 * [source,java]
 * .Example of find options
 * --
 *
 *  // find with pagination options
 *  collection.find(FindOptions.limit(0, 10));
 *
 *  // find with sort options
 *  collection.find(FindOptions.sort("age", SortOrder.Descending));
 *
 *  // find with sort options and pagination options
 *  // this will first sort all records by age in descending order and
 *  // then it will take first 10 records as a result
 *  collection.find(sort("age", SortOrder.Descending).thenLimit(0, 10));
 *
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see NitriteCollection#find(FindOptions)
 * @see NitriteCollection#find(Filter, FindOptions)
 */
public class FindOptions {
    /**
     * Gets the offset for pagination in find operation.
     *
     * @return the offset for pagination.
     * */
    @Getter @NonFinal private int offset;

    /**
     * Gets the number of records in each page for pagination in
     * find operation results.
     *
     * @return the record size per page.
     * */
    @Getter @NonFinal private int size;

    /**
     * Gets the target value name for sorting the find results.
     *
     * @return the target value name.
     * */
    @Getter @NonFinal private String field;

    /**
     * Gets the sort order of the find result.
     *
     * @return the sort order.
     * */
    @Getter @NonFinal private SortOrder sortOrder = SortOrder.Ascending;

    /**
     * Gets the `null` values order of the find result.
     *
     * @return the `null` values order.
     * */
    @Getter @NonFinal private NullOrder nullOrder = NullOrder.Default;


    /**
     * Instantiates a new find options with pagination criteria.
     *
     * @param offset the pagination offset.
     * @param size   the number of records per page.
     */
    public FindOptions(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    /**
     * Instantiates a new find options with sorting criteria.
     *
     * @param field     the value to sort by.
     * @param sortOrder the sort order.
     */
    public FindOptions(String field, SortOrder sortOrder) {
        this.field = field;
        this.sortOrder = sortOrder;
    }

    /**
     * Instantiates a new find options with sorting criteria and `null` value order.
     *
     * @param field     the value to sort by.
     * @param sortOrder the sort order.
     * @param nullOrder the `null` value order.
     */
    public FindOptions(String field, SortOrder sortOrder, NullOrder nullOrder) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.nullOrder = nullOrder;
    }

    /**
     * Creates a find options with pagination criteria.
     *
     * @param offset the pagination offset.
     * @param size   the number of records per page.
     * @return the find options with pagination criteria.
     */
    public static FindOptions limit(int offset, int size) {
        return new FindOptions(offset, size);
    }

    /**
     * Creates a find options with sorting criteria.
     *
     * @param field     the value to sort by.
     * @param sortOrder the sort order.
     * @return the find options with sorting criteria.
     */
    public static FindOptions sort(String field, SortOrder sortOrder) {
        return new FindOptions(field, sortOrder);
    }

    /**
     * Creates a find options with sorting criteria.
     *
     * @param field     the value to sort by.
     * @param sortOrder the sort order.
     * @param nullOrder the `null` value order.
     * @return the find options with sorting criteria.
     */
    public static FindOptions sort(String field, SortOrder sortOrder, NullOrder nullOrder) {
        return new FindOptions(field, sortOrder, nullOrder);
    }

    /**
     * Sets the pagination criteria of a @{@link FindOptions} with sorting updateOptions.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: With this {@link FindOptions} it will first sort all search results,
     * then it will apply pagination criteria on the sorted results.
     *
     * @param offset the pagination offset.
     * @param size   the number of records per page.
     * @return the find updateOptions with pagination and sorting criteria.
     */
    public FindOptions thenLimit(int offset, int size) {
        this.offset = offset;
        this.size = size;
        return this;
    }
}
