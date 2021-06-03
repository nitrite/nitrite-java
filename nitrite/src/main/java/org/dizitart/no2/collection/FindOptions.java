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
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Data
@Accessors(fluent = true, chain = true)
@Setter(AccessLevel.PACKAGE)
public class FindOptions {
    private SortableFields orderBy;
    private Long skip;
    private Long limit;

    @Setter(AccessLevel.PUBLIC)
    private Collator collator;

    public FindOptions() {
        this.collator = Collator.getInstance();
    }

    public static FindOptions orderBy(String fieldName, SortOrder sortOrder) {
        SortableFields fields = new SortableFields();
        fields.addField(fieldName, sortOrder);

        FindOptions findOptions = new FindOptions();
        findOptions.orderBy(fields);
        return findOptions;
    }

    public static FindOptions skipBy(long skip) {
        FindOptions findOptions = new FindOptions();
        findOptions.skip(skip);
        return findOptions;
    }

    public static FindOptions limitBy(long limit) {
        FindOptions findOptions = new FindOptions();
        findOptions.limit(limit);
        return findOptions;
    }

    public FindOptions skip(Long skip) {
        this.skip = skip;
        return this;
    }

    public FindOptions skip(Integer skip) {
        this.skip = skip == null ? null : (long) skip;
        return this;
    }

    public FindOptions limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public FindOptions limit(Integer limit) {
        this.limit = limit == null ? null : (long) limit;
        return this;
    }

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
}
