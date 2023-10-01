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

import lombok.Data;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.FieldBasedFilter;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.index.IndexDescriptor;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A plan for finding documents in a collection.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
public class FindPlan {
    /**
     * Gets the {@link FieldBasedFilter} for byId search if any.
     * */
    private FieldBasedFilter byIdFilter;

    /**
     * Gets the {@link IndexScanFilter} for index scan if any.
     * */
    private IndexScanFilter indexScanFilter;

    /**
     * Gets the {@link Filter} for collection scan if any.
     * */
    private Filter collectionScanFilter;

    /**
     * Gets the {@link IndexDescriptor} for index scan if any.
     * */
    private IndexDescriptor indexDescriptor;

    /**
     * Gets the index scan order.
     * */
    private Map<String, Boolean> indexScanOrder;

    /**
     * Gets the blocking sort order.
     * */
    private List<Pair<String, SortOrder>> blockingSortOrder;

    /**
     * Gets the skip count.
     * */
    private Long skip;

    /**
     * Gets the limit count.
     * */
    private Long limit;

    /**
     * Gets the distinct flag.
     * */
    private boolean distinct;

    /**
     * Gets the {@link Collator}.
     * */
    private Collator collator;

    /**
     * Gets the sub plans.
     * */
    private List<FindPlan> subPlans;

    /**
     * Instantiates a new {@link FindPlan}.
     */
    public FindPlan() {
        this.subPlans = new ArrayList<>();
        this.blockingSortOrder = new ArrayList<>();
    }
}
