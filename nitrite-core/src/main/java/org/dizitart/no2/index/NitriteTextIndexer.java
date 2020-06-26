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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;
import static org.dizitart.no2.common.util.StringUtils.stringTokenizer;
import static org.dizitart.no2.common.util.ValidationUtils.*;

/**
 * @author Anindya Chatterjee
 */
@SuppressWarnings("rawtypes")
public class NitriteTextIndexer implements TextIndexer {
    private final TextTokenizer textTokenizer;
    private IndexCatalog indexCatalog;
    private NitriteStore nitriteStore;

    public NitriteTextIndexer() {
        this.textTokenizer = new EnglishTextTokenizer();
    }

    public NitriteTextIndexer(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
    }

    @Override
    public String getIndexType() {
        return IndexType.Fulltext;
    }

    @Override
    public Set<NitriteId> findText(String collectionName, String field, String searchString) {
        notNull(field, "field cannot be null");
        notNull(searchString, "search term cannot be null");

        try {
            if (searchString.startsWith("*") || searchString.endsWith("*")) {
                return searchByWildCard(collectionName, field, searchString);
            } else {
                return searchExactByIndex(collectionName, field, searchString);
            }
        } catch (IOException ioe) {
            throw new IndexingException("could not search on full-text index", ioe);
        }
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        createOrUpdate(collection, nitriteId, field, fieldValue);
    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        try {
            validateStringValue(fieldValue, field);
            Set<String> words = decompose(fieldValue);

            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = getIndexMap(collection.getName(), field);

            for (String word : words) {
                ConcurrentSkipListSet<NitriteId> nitriteIds = indexMap.get(word);
                if (nitriteIds != null) {
                    nitriteIds.remove(nitriteId);

                    if (nitriteIds.isEmpty()) {
                        indexMap.remove(word);
                    } else {
                        indexMap.put(word, nitriteIds);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IndexingException("failed to remove full-text index data for " + field + " with id " + nitriteId);
        }
    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {
        removeIndex(collection, nitriteId, field, oldValue);
        createOrUpdate(collection, nitriteId, field, newValue);
    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {
        indexCatalog.dropIndexEntry(collection.getName(), field);
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = this.nitriteStore.getIndexCatalog();
    }

    @SuppressWarnings("rawtypes")
    private NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String collectionName, String field) {
        String mapName = getIndexMapName(collectionName, field);
        return nitriteStore.openMap(mapName);
    }

    private void validateStringValue(Object value, String field) {
        if (value == null || value instanceof String) return;

        if (value instanceof Iterable) {
            validateStringIterableIndexField((Iterable) value, field);
        } else if (value.getClass().isArray()) {
            validateStringArrayIndexField(value, field);
        } else {
            throw new IndexingException("string data is expected");
        }
    }

    private void createOrUpdate(NitriteMap<NitriteId, Document> collection, NitriteId id, String field, Object fieldValue) {
        try {
            validateStringValue(fieldValue, field);
            Set<String> words = decompose(fieldValue);

            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = getIndexMap(collection.getName(), field);

            for (String word : words) {
                ConcurrentSkipListSet<NitriteId> nitriteIds = indexMap.get(word);

                if (nitriteIds == null) {
                    nitriteIds = new ConcurrentSkipListSet<>();
                }
                nitriteIds.add(id);
                indexMap.put(word, nitriteIds);
            }
        } catch (IOException ioe) {
            throw new IndexingException("could not write full-text index data for " + fieldValue, ioe);
        }
    }

    private Set<String> decompose(Object fieldValue) throws IOException {
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

    private Set<NitriteId> searchByWildCard(String collectionName, String field, String searchString) {
        if (searchString.contentEquals("*")) {
            throw new FilterException("* is not a valid search string");
        }

        StringTokenizer stringTokenizer = stringTokenizer(searchString);
        if (stringTokenizer.countTokens() > 1) {
            throw new FilterException("multiple words with wildcard is not supported");
        }

        if (searchString.startsWith("*") && !searchString.endsWith("*")) {
            return searchByLeadingWildCard(collectionName, field, searchString);
        } else if (searchString.endsWith("*") && !searchString.startsWith("*")) {
            return searchByTrailingWildCard(collectionName, field, searchString);
        } else {
            String term = searchString.substring(1, searchString.length() - 1);
            return searchContains(collectionName, field, term);
        }
    }

    private Set<NitriteId> searchByTrailingWildCard(String collectionName, String field, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("invalid search term '*'");
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
            = getIndexMap(collectionName, field);
        Set<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(0, searchString.length() - 1);

        for (KeyValuePair<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entries()) {
            String key = (String) entry.getKey();
            if (key.startsWith(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchContains(String collectionName, String field, String term) {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
            = getIndexMap(collectionName, field);
        Set<NitriteId> idSet = new LinkedHashSet<>();

        for (KeyValuePair<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entries()) {
            String key = (String) entry.getKey();
            if (key.contains(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchByLeadingWildCard(String collectionName, String field, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("invalid search term '*'");
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
            = getIndexMap(collectionName, field);
        Set<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(1);

        for (KeyValuePair<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entries()) {
            String key = (String) entry.getKey();
            if (key.endsWith(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchExactByIndex(String collectionName, String field, String searchString) throws IOException {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
            = getIndexMap(collectionName, field);

        Set<String> words = textTokenizer.tokenize(searchString);
        Map<NitriteId, Integer> scoreMap = new HashMap<>();
        for (String word : words) {
            ConcurrentSkipListSet<NitriteId> nitriteIds = indexMap.get(word);
            if (nitriteIds != null) {
                for (NitriteId id : nitriteIds) {
                    Integer score = scoreMap.get(id);
                    if (score == null) {
                        scoreMap.put(id, 1);
                    } else {
                        scoreMap.put(id, score + 1);
                    }
                }
            }
        }

        Map<NitriteId, Integer> sortedScoreMap = sortByScore(scoreMap);
        return sortedScoreMap.keySet();
    }

    private <K, V extends Comparable<V>> Map<K, V> sortByScore(Map<K, V> unsortedMap) {
        List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (e1, e2) -> (e2.getValue()).compareTo(e1.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
