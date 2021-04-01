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

package org.dizitart.no2.index;

import lombok.Getter;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.TextFilter;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;
import static org.dizitart.no2.common.util.ValidationUtils.validateStringArrayIndexField;
import static org.dizitart.no2.common.util.ValidationUtils.validateStringIterableIndexField;

/**
 * Represents a nitrite full-text index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class TextIndex implements NitriteIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final NitriteStore<?> nitriteStore;
    private final TextTokenizer textTokenizer;

    /**
     * Instantiates a new {@link TextIndex}.
     *
     * @param textTokenizer   the text tokenizer
     * @param indexDescriptor the index descriptor
     * @param nitriteStore    the nitrite store
     */
    public TextIndex(TextTokenizer textTokenizer,
                     IndexDescriptor indexDescriptor,
                     NitriteStore<?> nitriteStore) {
        this.textTokenizer = textTokenizer;
        this.indexDescriptor = indexDescriptor;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            addIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof String) {
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            addIndexElement(indexMap, fieldValues, (String) element);
        } else if (element.getClass().isArray()) {
            validateStringArrayIndexField(element, firstField);

            Object[] array = convertToObjectArray(element);
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            for (Object item : array) {
                addIndexElement(indexMap, fieldValues, (String) item);
            }
        } else if (element instanceof Iterable) {
            validateStringIterableIndexField((Iterable<?>) element, firstField);

            Iterable<?> iterable = (Iterable<?>) element;
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            for (Object item : iterable) {
                addIndexElement(indexMap, fieldValues, (String) item);
            }
        } else {
            throw new IndexingException("string data is expected");
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            removeIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof String) {
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            removeIndexElement(indexMap, fieldValues, (String) element);
        } else if (element.getClass().isArray()) {
            validateStringArrayIndexField(element, firstField);

            Object[] array = convertToObjectArray(element);
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            for (Object item : array) {
                removeIndexElement(indexMap, fieldValues, (String) item);
            }
        } else if (element instanceof Iterable) {
            validateStringIterableIndexField((Iterable<?>) element, firstField);

            Iterable<?> iterable = (Iterable<?>) element;
            NitriteMap<String, List<?>> indexMap = findIndexMap();

            for (Object item : iterable) {
                removeIndexElement(indexMap, fieldValues, (String) item);
            }
        } else {
            throw new IndexingException("string data is expected");
        }
    }

    @Override
    public void drop() {
        NitriteMap<String, List<?>> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        NitriteMap<String, List<?>> indexMap = findIndexMap();
        List<Filter> filters = findPlan.getIndexScanFilter().getFilters();

        if (filters.size() == 1 && filters.get(0) instanceof TextFilter) {
            TextFilter textFilter = (TextFilter) filters.get(0);
            textFilter.setTextTokenizer(textTokenizer);
            return textFilter.applyOnIndex(indexMap);
        }
        throw new FilterException("invalid filter found for full-text index");
    }

    private NitriteMap<String, List<?>> findIndexMap() {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, String.class, ArrayList.class);
    }

    @SuppressWarnings("unchecked")
    private void addIndexElement(NitriteMap<String, List<?>> indexMap, FieldValues fieldValues, String value) {
        Set<String> words = decompose(value);

        for (String word : words) {
            List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(word);

            if (nitriteIds == null) {
                nitriteIds = new ArrayList<>();
            }

            nitriteIds = addNitriteIds(nitriteIds, fieldValues);
            indexMap.put(value, nitriteIds);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeIndexElement(NitriteMap<String, List<?>> indexMap, FieldValues fieldValues, String value) {
        Set<String> words = decompose(value);
        for (String word : words) {
            List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(word);
            if (nitriteIds != null && !nitriteIds.isEmpty()) {
                nitriteIds.remove(fieldValues.getNitriteId());
                if (nitriteIds.isEmpty()) {
                    indexMap.remove(word);
                } else {
                    indexMap.put(word, nitriteIds);
                }
            }
        }
    }

    private Set<String> decompose(Object fieldValue) {
        Set<String> result = new HashSet<>();
        if (fieldValue == null) {
            result.add(null);
        } else if (fieldValue instanceof String) {
            result.add((String) fieldValue);
        } else if (fieldValue instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) fieldValue;
            for (Object item : iterable) {
                result.addAll(decompose(item));
            }
        } else if (fieldValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(fieldValue);
            for (Object item : array) {
                result.addAll(decompose(item));
            }
        }

        Set<String> words = new HashSet<>();
        for (String item : result) {
            words.addAll(textTokenizer.tokenize(item));
        }

        return words;
    }
}
