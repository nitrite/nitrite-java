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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.*;
import org.dizitart.no2.index.IndexDescriptor;

import java.util.*;

import static org.dizitart.no2.common.Constants.DOC_ID;
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
            findPlan.setDistinct(findOptions.distinct());
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
        FindPlan findPlan = new FindPlan();
        Set<ComparableFilter> indexScanFilters = new LinkedHashSet<>();
        Set<Filter> columnScanFilters = new LinkedHashSet<>();

        // find out set id filter (if any)
        planForIdFilter(findPlan, filters);

        // find out if there are any index only filter with index
        planForIndexOnlyFilters(findPlan, indexScanFilters, indexDescriptors, filters);

        // if no id filter found or no index only filter found, scan for matching index
        if (findPlan.getByIdFilter() == null && indexScanFilters.isEmpty()) {
            planForIndexScanningFilters(findPlan, indexScanFilters, indexDescriptors, filters);
        }

        // plan for column scan filters
        planForCollectionScanningFilters(findPlan, indexScanFilters, columnScanFilters, filters);

        IndexScanFilter indexScanFilter;
        if (indexScanFilters.size() == 1) {
            indexScanFilter = new IndexScanFilter(Collections.singletonList(firstOrNull(indexScanFilters)));
            findPlan.setIndexScanFilter(indexScanFilter);
        } else if (indexScanFilters.size() > 1) {
            indexScanFilter = new IndexScanFilter(indexScanFilters);
            findPlan.setIndexScanFilter(indexScanFilter);
        }

        if (columnScanFilters.size() == 1) {
            findPlan.setCollectionScanFilter(firstOrNull(columnScanFilters));
        } else if (columnScanFilters.size() > 1) {
            Filter andFilter = and(columnScanFilters.toArray(new Filter[0]));
            findPlan.setCollectionScanFilter(andFilter);
        }

        return findPlan;
    }

    private void planForIdFilter(FindPlan findPlan, List<Filter> filters) {
        for (Filter filter : filters) {
            if (filter instanceof EqualsFilter) {
                EqualsFilter equalsFilter = (EqualsFilter) filter;

                // handle byId filter specially
                if (equalsFilter.getField().equals(DOC_ID)) {
                    findPlan.setByIdFilter(equalsFilter);
                }
                break;
            }
        }
    }

    private void planForIndexOnlyFilters(FindPlan findPlan, Set<ComparableFilter> indexScanFilters,
                                         Collection<IndexDescriptor> indexDescriptors, List<Filter> filters) {
        // find out if there are any filter which does not support covered queries
        List<IndexOnlyFilter> indexOnlyFilters = new ArrayList<>();
        for (Filter filter : filters) {
            if (filter instanceof IndexOnlyFilter) {
                IndexOnlyFilter indexScanFilter = (IndexOnlyFilter) filter;
                if (isCompatibleFilter(indexOnlyFilters, indexScanFilter)) {
                    // if filter is compatible with already identified index only filter then add
                    indexOnlyFilters.add(indexScanFilter);
                } else {
                    throw new FilterException("A query can not have multiple index only filters");
                }
            }
        }

        // populate index descriptor for the index only filters
        if (!indexOnlyFilters.isEmpty()) {

            // get any index only filter from the set
            IndexOnlyFilter anyFilter = indexOnlyFilters.get(0);
            for (IndexDescriptor indexDescriptor : indexDescriptors) {

                // check the index type match between filter and index descriptor
                if (anyFilter.supportedIndexType().equals(indexDescriptor.getIndexType())) {
                    // choose the index descriptor and filters
                    findPlan.setIndexDescriptor(indexDescriptor);
                    indexScanFilters.addAll(indexOnlyFilters);
                    break;
                }
            }

            if (findPlan.getIndexDescriptor() == null) {
                throw new FilterException(anyFilter.getField() + " is not indexed with "
                    + anyFilter.supportedIndexType() + " index");
            }
        }
    }

    private boolean isCompatibleFilter(List<IndexOnlyFilter> indexOnlyFilters, IndexOnlyFilter filter) {
        if (indexOnlyFilters.isEmpty()) {
            return true;
        } else {
            IndexOnlyFilter comparableFilter = indexOnlyFilters.get(0);
            return comparableFilter.canBeGrouped(filter);
        }
    }

    private void planForIndexScanningFilters(FindPlan findPlan, Set<ComparableFilter> indexScanFilters,
                                             Collection<IndexDescriptor> indexDescriptors, List<Filter> filters) {
        // descending sort based on cardinality of indices, consider the higher cardinality index first
        NavigableMap<IndexDescriptor, List<ComparableFilter>> indexFilterMap = new TreeMap<>(Collections.reverseOrder());

        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            List<String> fieldNames = indexDescriptor.getIndexFields().getFieldNames();

            List<ComparableFilter> indexedFilters = new ArrayList<>();
            for (String fieldName : fieldNames) {
                boolean matchFound = false;
                for (Filter filter : filters) {
                    if (filter instanceof ComparableFilter) {
                        String filterFieldName = ((ComparableFilter) filter).getField();
                        if (filterFieldName.equals(fieldName)) {
                            indexedFilters.add((ComparableFilter) filter);
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

        for (Map.Entry<IndexDescriptor, List<ComparableFilter>> entry : indexFilterMap.entrySet()) {
            // consider the filter combination if it encompasses more fields
            // than the previously selected filter
            if (entry.getValue().size() > indexScanFilters.size()) {
                // maintain the order in set
                indexScanFilters.addAll(entry.getValue());
                findPlan.setIndexDescriptor(entry.getKey());
            }
        }
    }

    private void planForCollectionScanningFilters(FindPlan findPlan, Set<ComparableFilter> indexScanFilters,
                                                  Set<Filter> columnScanFilters, List<Filter> filters) {
        for (Filter filter : filters) {
            // ignore the elected filters for index scan and
            // insert rest of the filters for column scan
            // NOTE: for byId filter, index scan filters will always be empty
            if (!(filter instanceof ComparableFilter) || !indexScanFilters.contains(filter)) {
                // ignore the byId filter (if any) for column scan
                if (filter != findPlan.getByIdFilter()) {
                    columnScanFilters.add(filter);
                }
            }
        }

        // validate whether column scanning is supported for each filter,
        // if there is no index scan available
        if (indexScanFilters.isEmpty()) {
            validateCollectionScanFilters(columnScanFilters);
        }
    }

    private void validateCollectionScanFilters(Collection<Filter> filters) {
        for (Filter filter : filters) {
            if (filter instanceof IndexOnlyFilter) {
                throw new FilterException("Collection scan is not supported for the filter " + filter);
            } else if (filter instanceof TextFilter) {
                throw new FilterException(((TextFilter) filter).getField() + " is not full-text indexed");
            }
        }
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
