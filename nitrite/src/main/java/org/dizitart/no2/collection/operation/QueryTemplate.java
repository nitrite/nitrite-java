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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.VE_INDEXED_QUERY_TEMPLATE_NULL;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.util.ValidationUtils.notNull;
import static org.dizitart.no2.util.ValidationUtils.validateLimit;

/**
 * @author Anindya Chatterjee.
 */
class QueryTemplate {
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMap<NitriteId, Document> underlyingMap;

    QueryTemplate(IndexedQueryTemplate indexedQueryTemplate,
                  NitriteMap<NitriteId, Document> mapStore) {
        notNull(indexedQueryTemplate, errorMessage("indexedQueryTemplate can not be null",
            VE_INDEXED_QUERY_TEMPLATE_NULL));
        this.indexedQueryTemplate = indexedQueryTemplate;
        this.underlyingMap = mapStore;
    }

    Document getById(NitriteId nitriteId) {
        return underlyingMap.get(nitriteId);
    }

    Cursor find() {
        FindResult findResult = new FindResult();
        findResult.setHasMore(false);
        findResult.setTotalCount(underlyingMap.size());
        findResult.setIdSet(underlyingMap.keySet());
        findResult.setUnderlyingMap(underlyingMap);

        return new DocumentCursor(findResult);
    }

    Cursor find(Filter filter) {
        if (filter == null) {
            return find();
        }
        filter.setIndexedQueryTemplate(indexedQueryTemplate);
        Set<NitriteId> result;

        try {
            result = filter.apply(underlyingMap);
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(FILTERED_FIND_OPERATION_FAILED, t);
        }

        FindResult findResult = new FindResult();
        findResult.setUnderlyingMap(underlyingMap);
        if (result != null) {
            findResult.setHasMore(false);
            findResult.setTotalCount(result.size());
            findResult.setIdSet(result);
        }

        return new DocumentCursor(findResult);
    }

    Cursor find(FindOptions findOptions) {
        FindResult findResult = new FindResult();
        findResult.setUnderlyingMap(underlyingMap);
        setUnfilteredResultSet(findOptions, findResult);

        return new DocumentCursor(findResult);
    }

    Cursor find(Filter filter, FindOptions findOptions) {
        if (filter == null) {
            return find(findOptions);
        }
        filter.setIndexedQueryTemplate(indexedQueryTemplate);
        FindResult findResult = new FindResult();
        findResult.setUnderlyingMap(underlyingMap);
        setFilteredResultSet(filter, findOptions, findResult);

        return new DocumentCursor(findResult);
    }

    private void setUnfilteredResultSet(FindOptions findOptions, FindResult findResult) {
        validateLimit(findOptions, underlyingMap.sizeAsLong());

        Set<NitriteId> resultSet;
        if (isNullOrEmpty(findOptions.getField())) {
            resultSet = limitIdSet(underlyingMap.keySet(), findOptions);
        } else {
            resultSet = sortIdSet(underlyingMap.keySet(), findOptions);
        }

        findResult.setIdSet(resultSet);
        findResult.setTotalCount(underlyingMap.size());
        findResult.setHasMore(underlyingMap.keySet().size() > (findOptions.getSize() + findOptions.getOffset()));
    }

    private void setFilteredResultSet(Filter filter, FindOptions findOptions, FindResult findResult) {
        Set<NitriteId> nitriteIdSet;
        try {
            nitriteIdSet = filter.apply(underlyingMap);
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(FILTERED_FIND_WITH_OPTIONS_OPERATION_FAILED, t);
        }

        if (nitriteIdSet == null || nitriteIdSet.isEmpty()) return;

        validateLimit(findOptions, nitriteIdSet.size());
        Set<NitriteId> resultSet;

        if (isNullOrEmpty(findOptions.getField())) {
            resultSet = limitIdSet(nitriteIdSet, findOptions);
        } else {
            resultSet = sortIdSet(nitriteIdSet, findOptions);
        }

        findResult.setIdSet(resultSet);
        findResult.setHasMore(nitriteIdSet.size() > (findOptions.getSize() + findOptions.getOffset()));
        findResult.setTotalCount(nitriteIdSet.size());
    }

    private Set<NitriteId> sortIdSet(Collection<NitriteId> nitriteIdSet, FindOptions findOptions) {
        String sortField = findOptions.getField();
        NavigableMap<Object, List<NitriteId>> sortedMap = new TreeMap<>();
        Set<NitriteId> nullValueIds = new HashSet<>();

        for (NitriteId id : nitriteIdSet) {
            Document document = underlyingMap.get(id);
            Object value = getFieldValue(document, sortField);

            if (value != null) {
                if (value.getClass().isArray() || value instanceof Iterable) {
                    throw new InvalidOperationException(UNABLE_TO_SORT_ON_ARRAY);
                }
            } else {
                nullValueIds.add(id);
                continue;
            }

            if (sortedMap.containsKey(value)) {
                List<NitriteId> idList = sortedMap.get(value);
                idList.add(id);
                sortedMap.put(value, idList);
            } else {
                List<NitriteId> idList = new ArrayList<>();
                idList.add(id);
                sortedMap.put(value, idList);
            }
        }

        List<NitriteId> sortedValues;
        if (findOptions.getSortOrder() == SortOrder.Ascending) {
            if (findOptions.getNullOrder() == NullOrder.Default || findOptions.getNullOrder() == NullOrder.First) {
                sortedValues = new ArrayList<>(nullValueIds);
                sortedValues.addAll(flattenList(sortedMap.values()));
            } else {
                sortedValues = flattenList(sortedMap.values());
                sortedValues.addAll(nullValueIds);
            }
        } else {
            if (findOptions.getNullOrder() == NullOrder.Default || findOptions.getNullOrder() == NullOrder.Last) {
                sortedValues = flattenList(sortedMap.descendingMap().values());
                sortedValues.addAll(nullValueIds);
            } else {
                sortedValues = new ArrayList<>(nullValueIds);
                sortedValues.addAll(flattenList(sortedMap.descendingMap().values()));
            }
        }

        return limitIdSet(sortedValues, findOptions);
    }

    private Set<NitriteId> limitIdSet(Collection<NitriteId> nitriteIdSet, FindOptions findOptions) {
        int offset = findOptions.getOffset();
        int size = findOptions.getSize();
        Set<NitriteId> resultSet = new LinkedHashSet<>();

        int index = 0;
        for (NitriteId nitriteId : nitriteIdSet) {
            if (index >= offset) {
                resultSet.add(nitriteId);
                if (index == (offset + size - 1)) break;
            }
            index++;
        }

        return resultSet;
    }

    private <T> List<T> flattenList(Collection<List<T>> collection) {
        List<T> finalList = new ArrayList<>();
        for (List<T> list : collection) {
            finalList.addAll(list);
        }
        return finalList;
    }
}
