/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.SortableFields;

import java.text.Collator;

/**
 * The options for find operation.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Data
@Accessors(fluent = true, chain = true)
@Setter(AccessLevel.PACKAGE)
public class FindOptions {
    private SortableFields orderBy;
    private Long skip;
    private Long limit;
    private boolean distinct = false;

    /**
     * Specifies the {@link Collator}.
     *
     * @return the collator.
     */
    @Setter(AccessLevel.PUBLIC)
    private Collator collator;

    /**
     * Instantiates a new FindOptions.
     */
    public FindOptions() {
        this.collator = Collator.getInstance();
    }

    /**
     * Order by find options.
     *
     * @param fieldName the field name
     * @param sortOrder the sort order
     * @return the find options
     */
    public static FindOptions orderBy(String fieldName, SortOrder sortOrder) {
        SortableFields fields = new SortableFields();
        fields.addField(fieldName, sortOrder);

        FindOptions findOptions = new FindOptions();
        findOptions.orderBy(fields);
        return findOptions;
    }

    /**
     * Skip by find options.
     *
     * @param skip the skip
     * @return the find options
     */
    public static FindOptions skipBy(long skip) {
        FindOptions findOptions = new FindOptions();
        findOptions.skip(skip);
        return findOptions;
    }

    /**
     * Limit by find options.
     *
     * @param limit the limit
     * @return the find options
     */
    public static FindOptions limitBy(long limit) {
        FindOptions findOptions = new FindOptions();
        findOptions.limit(limit);
        return findOptions;
    }

    /**
     * Indicates if the find operation should return distinct results.
     */
    public static FindOptions withDistinct() {
        FindOptions findOptions = new FindOptions();
        findOptions.distinct(true);
        return findOptions;
    }

    /**
     * Skip find options.
     *
     * @param skip the skip
     * @return the find options
     */
    public FindOptions skip(Long skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Skip find options.
     *
     * @param skip the skip
     * @return the find options
     */
    public FindOptions skip(Integer skip) {
        this.skip = skip == null ? null : (long) skip;
        return this;
    }

    /**
     * Limit find options.
     *
     * @param limit the limit
     * @return the find options
     */
    public FindOptions limit(Long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Limit find options.
     *
     * @param limit the limit
     * @return the find options
     */
    public FindOptions limit(Integer limit) {
        this.limit = limit == null ? null : (long) limit;
        return this;
    }

    /**
     * Then order by find options.
     *
     * @param fieldName the field name
     * @param sortOrder the sort order
     * @return the find options
     */
    public FindOptions thenOrderBy(String fieldName, SortOrder sortOrder) {
        if (orderBy != null) {
            orderBy.addField(fieldName, sortOrder);
        } else {
            SortableFields fields = new SortableFields();
            fields.addField(fieldName, sortOrder);
            orderBy = fields;
        }
        return this;
    }

    /**
     * Indicates if the find operation should return distinct and unique results.
     *
     * @param distinct the distinct
     * @return the find options
     */
    public FindOptions withDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }
}
