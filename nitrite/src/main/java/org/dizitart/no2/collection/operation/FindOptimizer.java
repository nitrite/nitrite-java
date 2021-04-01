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

import static org.dizitart.no2.filters.Filter.and;


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
            findPlan.setNullOrder(findOptions.nullOrder());
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
        for (Filter filter : filters) {
            if (filter instanceof AndFilter || filter instanceof OrFilter) {
                findPlan.getSubPlans().clear();
                return findPlan;
            }

            FindPlan subPlan = createFilterPlan(indexDescriptors, filter);
            findPlan.getSubPlans().add(subPlan);
        }

        for (FindPlan plan : findPlan.getSubPlans()) {
            if (plan.getIndexDescriptor() == null) {
                findPlan.getSubPlans().clear();
                return findPlan;
            }
        }
        return findPlan;
    }

    private FindPlan createAndPlan(Collection<IndexDescriptor> indexDescriptors, List<Filter> filters) {
        Map<IndexDescriptor, List<Filter>> indexFilterMap = new HashMap<>();

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
                    break;
                }
            }

            if (!indexedFilters.isEmpty()) {
                indexFilterMap.put(indexDescriptor, indexedFilters);
            }
        }

        FindPlan findPlan = new FindPlan();
        List<Filter> electedFilters = new ArrayList<>();
        for (Map.Entry<IndexDescriptor, List<Filter>> entry : indexFilterMap.entrySet()) {
            if (entry.getValue().size() > electedFilters.size()) {
                electedFilters = entry.getValue();
                findPlan.setIndexDescriptor(entry.getKey());
            }
        }

        List<Filter> nonElectedFilters = new ArrayList<>();
        for (Filter filter : filters) {
            if (!electedFilters.contains(filter)) {
                nonElectedFilters.add(filter);
            }
        }

        IndexScanFilter indexScanFilter;
        if (electedFilters.size() == 1) {
            indexScanFilter = new IndexScanFilter(Collections.singletonList(electedFilters.get(0)));
            findPlan.setIndexScanFilter(indexScanFilter);
        } else if (electedFilters.size() > 1) {
            indexScanFilter =  new IndexScanFilter(electedFilters);
            findPlan.setIndexScanFilter(indexScanFilter);
        }

        if (nonElectedFilters.size() == 1) {
            findPlan.setCollectionScanFilter(nonElectedFilters.get(0));
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

                // get prefix length
                int length = Math.min(indexedFieldNames.size(), findSortSpec.size());

                boolean canUseIndex = false;
                Map<String, Boolean> indexScanOrder = new HashMap<>();
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

                if (canUseIndex) {
                    findPlan.setIndexScanOrder(indexScanOrder);
                    if (length < findSortSpec.size()) {
                        List<Pair<String, SortOrder>> remainder = findSortSpec.subList(length, findSortSpec.size() - 1);
                        findPlan.setBlockingSortOrder(remainder);
                    }
                } else {
                    findPlan.setBlockingSortOrder(findSortSpec);
                }
            }
            // no find options, so consider the index sorting order
            findPlan.setBlockingSortOrder(findSortSpec);
        }
    }

    private void readLimitOption(FindOptions findOptions, FindPlan findPlan) {
        if (findOptions != null) {
            findPlan.setLimit(findOptions.limit());
            findPlan.setSkip(findOptions.skip());
        }
    }
}
