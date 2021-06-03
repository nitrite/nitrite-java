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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.*;
import org.dizitart.no2.index.IndexDescriptor;

import java.util.*;

import static org.dizitart.no2.common.util.Iterables.firstOrNull;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.Filter.or;


/**
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
class FindOptimizer {

    public FindPlan optimize(Filter filter,
                             FindOptions findOptions,
                             Collection<IndexDescriptor> indexDescriptors) {
        FindPlan findPlan = createFilterPlan(indexDescriptors, filter);
        readSortOption(findOptions, findPlan);
        readLimitOption(findOptions, findPlan);

        if (findOptions != null) {
            findPlan.setCollator(findOptions.collator());
        }
        return findPlan;
    }

    private FindPlan createFilterPlan(Collection<IndexDescriptor> indexDescriptors, Filter filter) {
        if (filter instanceof AndFilter) {
            List<Filter> filters = flattenAndFilter((AndFilter) filter);
            return createAndPlan(indexDescriptors, filters);
        } else if (filter instanceof OrFilter) {
            return createOrPlan(indexDescriptors, ((OrFilter) filter).getFilters());
        } else {
            List<Filter> filters = Collections.singletonList(filter);
            return createAndPlan(indexDescriptors, filters);
        }
    }

    private FindPlan createOrPlan(Collection<IndexDescriptor> indexDescriptors, List<Filter> filters) {
        FindPlan findPlan = new FindPlan();

        Set<Filter> flattenedFilter = new HashSet<>();

        // flatten the or filter
        for (Filter filter : filters) {
            if (filter instanceof OrFilter) {
                flattenedFilter.addAll(((OrFilter) filter).getFilters());
            } else {
                flattenedFilter.add(filter);
            }
        }

        for (Filter filter : flattenedFilter) {
            FindPlan subPlan = createFilterPlan(indexDescriptors, filter);
            findPlan.getSubPlans().add(subPlan);
        }

        // check if all sub plan have index support
        for (FindPlan plan : findPlan.getSubPlans()) {
            if (plan.getIndexDescriptor() == null) {
                // if one of the sub plan doesn't have any index support
                // then it can not be optimized, instead the
                // original filter should be set as coll-scan filter
                // for the parent plan
                findPlan.getSubPlans().clear();
                // set the original or filter as coll scan filter
                findPlan.setCollectionScanFilter(or(filters.toArray(new Filter[0])));
                return findPlan;
            }
        }
        return findPlan;
    }

    private FindPlan createAndPlan(Collection<IndexDescriptor> indexDescriptors, List<Filter> filters) {
        // descending sort based on cardinality of indices, consider the higher cardinality index first
        NavigableMap<IndexDescriptor, List<Filter>> indexFilterMap = new TreeMap<>(Collections.reverseOrder());

        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            List<String> fieldNames = indexDescriptor.getIndexFields().getFieldNames();

            List<Filter> indexedFilters = new ArrayList<>();
            for (String fieldName : fieldNames) {
                boolean matchFound = false;
                for (Filter filter : filters) {
                    if (filter instanceof FieldBasedFilter) {
                        String filterFieldName = ((FieldBasedFilter) filter).getField();
                        if (filterFieldName.equals(fieldName)) {
                            indexedFilters.add(filter);
                            matchFound = true;
                            break;
                        }
                    }
                }

                if (!matchFound) {
                    // match not found, so can't consider this index
                    break;
                }
            }

            if (!indexedFilters.isEmpty()) {
                indexFilterMap.put(indexDescriptor, indexedFilters);
            }
        }

        FindPlan findPlan = new FindPlan();
        Set<Filter> electedFilters = new HashSet<>();
        for (Map.Entry<IndexDescriptor, List<Filter>> entry : indexFilterMap.entrySet()) {
            // consider the filter combination if it encompasses more fields
            // than the previously selected filter
            if (entry.getValue().size() > electedFilters.size()) {
                // maintain the order in set
                electedFilters = new LinkedHashSet<>(entry.getValue());
                findPlan.setIndexDescriptor(entry.getKey());
            }
        }

        // maintain the order in set
        Set<Filter> nonElectedFilters = new LinkedHashSet<>();
        for (Filter filter : filters) {
            if (!electedFilters.contains(filter)) {
                nonElectedFilters.add(filter);
            }
        }

        IndexScanFilter indexScanFilter;
        if (electedFilters.size() == 1) {
            indexScanFilter = new IndexScanFilter(Collections.singletonList(firstOrNull(electedFilters)));
            findPlan.setIndexScanFilter(indexScanFilter);
        } else if (electedFilters.size() > 1) {
            indexScanFilter =  new IndexScanFilter(electedFilters);
            findPlan.setIndexScanFilter(indexScanFilter);
        }

        if (nonElectedFilters.size() == 1) {
            findPlan.setCollectionScanFilter(firstOrNull(nonElectedFilters));
        } else if (nonElectedFilters.size() > 1) {
            Filter andFilter = and(nonElectedFilters.toArray(new Filter[0]));
            findPlan.setCollectionScanFilter(andFilter);
        }

        return findPlan;
    }

    private List<Filter> flattenAndFilter(AndFilter andFilter) {
        List<Filter> flattenedFilters = new ArrayList<>();
        if (andFilter != null) {
            for (Filter filter : andFilter.getFilters()) {
                if (filter instanceof AndFilter) {
                    List<Filter> filters = flattenAndFilter((AndFilter) filter);
                    flattenedFilters.addAll(filters);
                } else {
                    flattenedFilters.add(filter);
                }
            }
        }
        return flattenedFilters;
    }

    private void readSortOption(FindOptions findOptions, FindPlan findPlan) {
        IndexDescriptor indexDescriptor = findPlan.getIndexDescriptor();
        if (findOptions != null && findOptions.orderBy() != null) {
            // get sort spec for find
            List<Pair<String, SortOrder>> findSortSpec = findOptions.orderBy().getSortingOrders();

            if (indexDescriptor != null) {
                // get index field names
                List<String> indexedFieldNames = indexDescriptor.getIndexFields().getFieldNames();

                boolean canUseIndex = false;
                Map<String, Boolean> indexScanOrder = new HashMap<>();

                if (indexedFieldNames.size() >= findSortSpec.size()) {
                    // if all fields of the sort spec is covered by index, then only
                    // sorting can take help of index

                    int length = findSortSpec.size();
                    for (int i = 0; i < length; i++) {
                        String indexFieldName = indexedFieldNames.get(i);
                        Pair<String, SortOrder> findPair = findSortSpec.get(i);
                        if (!indexFieldName.equals(findPair.getFirst())) {
                            // field mismatch in sort spec, can't use index for sorting
                            canUseIndex = false;
                            break;
                        } else {
                            canUseIndex = true;
                            boolean reverseScan = false;

                            SortOrder findSortOrder = findPair.getSecond();
                            if (findSortOrder != SortOrder.Ascending) {
                                // if sort order is different, reverse scan in index
                                reverseScan = true;
                            }

                            // add to index scan order
                            indexScanOrder.put(indexFieldName, reverseScan);
                        }
                    }
                }

                if (canUseIndex) {
                    findPlan.setIndexScanOrder(indexScanOrder);
                } else {
                    findPlan.setBlockingSortOrder(findSortSpec);
                }
            } else {
                // no find options, so consider the index sorting order
                findPlan.setBlockingSortOrder(findSortSpec);
            }
        }
    }

    private void readLimitOption(FindOptions findOptions, FindPlan findPlan) {
        if (findOptions != null) {
            findPlan.setLimit(findOptions.limit());
            findPlan.setSkip(findOptions.skip());
        }
    }
}
