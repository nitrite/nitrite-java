/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.*;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexedQuerySupport;
import org.dizitart.no2.filters.NitriteFilter;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.common.util.Iterables.getElementType;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;
import static org.dizitart.no2.common.util.ValidationUtils.validateArrayIndexField;
import static org.dizitart.no2.common.util.ValidationUtils.validateIterableIndexField;

/**
 * @author Anindya Chatterjee
 */
@SuppressWarnings("rawtypes")
public abstract class ComparableIndexer extends BaseNitriteIndexer {
    protected IndexedQuerySupport querySupport;

    public ComparableIndexer() {
        querySupport = new IndexedQuerySupport(this);
    }

    abstract boolean isUnique();

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
    }

    @Override
    public RecordStream<NitriteId> findByFilter(String collectionName, Filter filter, NitriteConfig nitriteConfig) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            IndexCatalog indexCatalog = nitriteConfig.getNitriteStore().getIndexCatalog();
            Collection<IndexDescriptor> descriptors = indexCatalog.listIndexDescriptors(collectionName);


            FieldValues fieldValues = querySupport.calculateFieldValues(nitriteFilter);
            IndexDescriptor descriptor = new IndexDescriptor(getIndexType(), fieldValues.getFields(), collectionName);

            NitriteMap<?, ?> indexMap = findSuitableIndexMap(descriptor, fieldValues, nitriteConfig.getNitriteStore());


            return querySupport.findByFilter(indexMap, nitriteFilter, nitriteConfig);
        } else {
            throw new FilterException(filter.getClass().getName() + " is not supported");
        }
    }

    //region Write Index

    @Override
    public void writeIndexEntry(IndexDescriptor indexDescriptor,
                                FieldValues fieldValues,
                                NitriteConfig nitriteConfig) {
        if (isCompoundIndex(indexDescriptor)) {
            writeCompoundIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
        } else {
            writeSimpleIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
        }
    }

    @Override
    public void removeIndexEntry(IndexDescriptor indexDescriptor,
                                 FieldValues fieldValues,
                                 NitriteConfig nitriteConfig) {
        if (isCompoundIndex(indexDescriptor)) {
            removeCompoundIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
        } else {
            removeSimpleIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
        }
    }

    private void writeCompoundIndexEntry(IndexDescriptor indexDescriptor,
                                         FieldValues fieldValues,
                                         NitriteConfig nitriteConfig) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);

        if (firstValue == null) {
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap
                = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);

            addCompoundIndexElement(indexMap, fieldValues, null);
        } else if (firstValue instanceof Comparable) {
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap
                = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), firstValue.getClass());

            addCompoundIndexElement(indexMap, fieldValues, (Comparable) firstValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(firstValue);
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap = getCompoundIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), array.getClass().getComponentType());

            for (Object item : array) {
                addCompoundIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable iterable = (Iterable) firstValue;
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap = getCompoundIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), getElementType(iterable));

            for (Object item : iterable) {
                addCompoundIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        }
    }

    private void writeSimpleIndexEntry(IndexDescriptor indexDescriptor,
                                       FieldValues fieldValues,
                                       NitriteConfig nitriteConfig) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<Comparable, NavigableSet<?>> indexMap
                = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);

            addSimpleIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof Comparable) {
            NitriteMap<Comparable, NavigableSet<?>> indexMap
                = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), element.getClass());

            addSimpleIndexElement(indexMap, fieldValues, (Comparable) element);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);
            NitriteMap<Comparable, NavigableSet<?>> indexMap = getSimpleIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), array.getClass().getComponentType());

            for (Object item : array) {
                addSimpleIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        } else if (element instanceof Iterable) {
            Iterable iterable = (Iterable) element;
            NitriteMap<Comparable, NavigableSet<?>> indexMap = getSimpleIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), getElementType(iterable));

            for (Object item : iterable) {
                addSimpleIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        }
    }

    private void addCompoundIndexElement(NitriteMap<Comparable, NavigableMap<?, ?>> indexMap,
                                         FieldValues fieldValues, Comparable element) {
        NavigableMap<?, ?> subMap = indexMap.get(element);
        if (subMap == null) {
            SortOrder sortOrder = fieldValues.getFields().getFirstKey().getSecond();
            subMap = sortOrder == SortOrder.Ascending ? new ConcurrentSkipListMap<>()
                : new ConcurrentSkipListMap<>(Collections.reverseOrder());
        }

        populateSubMap(subMap, fieldValues, 1);
        indexMap.put(element, subMap);
    }

    @SuppressWarnings("unchecked")
    private void populateSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        for (int i = startIndex; i < fieldValues.getValues().size(); i++) {
            Pair<String, Object> pair = fieldValues.getValues().get(i);
            Object value = pair.getSecond();
            if (value == null) {
                value = DBNull.getInstance();
            }

            if (!(value instanceof Comparable)) {
                throw new IndexingException(value + " is not comparable");
            }

            if (i == fieldValues.getValues().size() - 1) {
                NavigableSet<NitriteId> nitriteIds = (NavigableSet<NitriteId>) subMap.get(value);
                nitriteIds = addNitriteIds(nitriteIds, fieldValues);
                subMap.put(value, nitriteIds);
            } else {
                NavigableMap subMap2 = (NavigableMap) subMap.get(value);
                if (subMap2 == null) {
                    SortOrder sortOrder = fieldValues.getFields().getSortOrder(pair.getFirst());
                    subMap2 = sortOrder == SortOrder.Ascending ? new ConcurrentSkipListMap<>()
                        : new ConcurrentSkipListMap<>(Collections.reverseOrder());
                }

                populateSubMap(subMap2, fieldValues, startIndex + 1);
                subMap.put(value, subMap2);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addSimpleIndexElement(NitriteMap<Comparable, NavigableSet<?>> indexMap,
                                       FieldValues fieldValues, Comparable element) {
        NavigableSet<NitriteId> nitriteIds = (NavigableSet<NitriteId>) indexMap.get(element);
        nitriteIds = addNitriteIds(nitriteIds, fieldValues);
        indexMap.put(element, nitriteIds);
    }

    private NavigableSet<NitriteId> addNitriteIds(NavigableSet<NitriteId> nitriteIds, FieldValues fieldValues) {
        if (nitriteIds == null) {
            nitriteIds = new ConcurrentSkipListSet<>();
        }

        if (isUnique() && nitriteIds.size() == 1
            && !nitriteIds.contains(fieldValues.getNitriteId())) {
            // if key is already exists for unique type, throw error
            throw new UniqueConstraintException("unique key constraint violation for " + fieldValues.getFields());
        }

        nitriteIds.add(fieldValues.getNitriteId());
        return nitriteIds;
    }

    private void removeCompoundIndexEntry(IndexDescriptor indexDescriptor,
                                          FieldValues fieldValues,
                                          NitriteConfig nitriteConfig) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);

        if (firstValue == null) {
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap
                = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);

            removeCompoundIndexElement(indexMap, fieldValues, null);
        } else if (firstValue instanceof Comparable) {
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap
                = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), firstValue.getClass());

            removeCompoundIndexElement(indexMap, fieldValues, (Comparable) firstValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(firstValue);
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap = getCompoundIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), array.getClass().getComponentType());

            for (Object item : array) {
                removeCompoundIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable iterable = (Iterable) firstValue;
            NitriteMap<Comparable, NavigableMap<?, ?>> indexMap = getCompoundIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), getElementType(iterable));

            for (Object item : iterable) {
                removeCompoundIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        }
    }

    private void removeSimpleIndexEntry(IndexDescriptor indexDescriptor,
                                        FieldValues fieldValues,
                                        NitriteConfig nitriteConfig) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<Comparable, NavigableSet<?>> indexMap
                = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);

            removeSimpleIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof Comparable) {
            NitriteMap<Comparable, NavigableSet<?>> indexMap
                = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), element.getClass());

            removeSimpleIndexElement(indexMap, fieldValues, (Comparable) element);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);
            NitriteMap<Comparable, NavigableSet<?>> indexMap = getSimpleIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), array.getClass().getComponentType());

            for (Object item : array) {
                removeSimpleIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        } else if (element instanceof Iterable) {
            Iterable iterable = (Iterable) element;
            NitriteMap<Comparable, NavigableSet<?>> indexMap = getSimpleIndexMap(indexDescriptor,
                nitriteConfig.getNitriteStore(), getElementType(iterable));

            for (Object item : iterable) {
                removeSimpleIndexElement(indexMap, fieldValues, (Comparable) item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void removeSimpleIndexElement(NitriteMap<Comparable, NavigableSet<?>> indexMap,
                                       FieldValues fieldValues, Comparable element) {
        NavigableSet<NitriteId> nitriteIds = (NavigableSet<NitriteId>) indexMap.get(element);
        if (nitriteIds != null && !nitriteIds.isEmpty()) {
            nitriteIds.remove(fieldValues.getNitriteId());
            if (nitriteIds.size() == 0) {
                indexMap.remove(element);
            } else {
                indexMap.put(element, nitriteIds);
            }
        }
    }

    private void removeCompoundIndexElement(NitriteMap<Comparable, NavigableMap<?, ?>> indexMap,
                                            FieldValues fieldValues, Comparable element) {
        NavigableMap<?, ?> subMap = indexMap.get(element);
        if (subMap != null && !subMap.isEmpty()) {
            deleteFromSubMap(subMap, fieldValues, 1);
            indexMap.put(element, subMap);
        }
    }

    @SuppressWarnings("unchecked")
    private void deleteFromSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        for (int i = startIndex; i < fieldValues.getValues().size(); i++) {
            Pair<String, Object> pair = fieldValues.getValues().get(i);
            Object value = pair.getSecond();
            if (value == null) {
                value = DBNull.getInstance();
            }

            if (!(value instanceof Comparable)) {
                continue;
            }

            if (i == fieldValues.getValues().size() - 1) {
                NavigableSet<NitriteId> nitriteIds = (NavigableSet<NitriteId>) subMap.get(value);
                nitriteIds = removeNitriteIds(nitriteIds, fieldValues);
                if (nitriteIds == null || nitriteIds.isEmpty()) {
                    subMap.remove(value);
                } else {
                    subMap.put(value, nitriteIds);
                }
            } else {
                NavigableMap subMap2 = (NavigableMap) subMap.get(value);
                if (subMap2 == null) {
                    continue;
                }

                deleteFromSubMap(subMap2, fieldValues, startIndex + 1);
                subMap.put(value, subMap2);
            }
        }
    }

    private NavigableSet<NitriteId> removeNitriteIds(NavigableSet<NitriteId> nitriteIds, FieldValues fieldValues) {
        if (nitriteIds != null && !nitriteIds.isEmpty()) {
            nitriteIds.remove(fieldValues.getNitriteId());
        }
        return nitriteIds;
    }

    private void validateIndexField(Object value, String field) {
        if (value == null) return;
        if (value instanceof Iterable) {
            validateIterableIndexField((Iterable) value, field);
        } else if (value.getClass().isArray()) {
            validateArrayIndexField(value, field);
        } else {
            if (!(value instanceof Comparable)) {
                throw new ValidationException(value + " is not comparable");
            }
        }
    }

    //endregion

    private NitriteMap<?, ?> findSuitableIndexMap(IndexDescriptor descriptor,
                                                  FieldValues fieldValues,
                                                  NitriteStore<?> nitriteStore) {
        // get all indices of a collection
        // find the suitable index (prefix included)
        // find the index map
        // get value by prefix using filter (if compound)
        // get value by filter (if simple)
        return null;
    }
}
