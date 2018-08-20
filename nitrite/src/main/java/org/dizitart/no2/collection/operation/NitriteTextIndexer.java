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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.IndexUtils.sortByScore;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee.
 */
class NitriteTextIndexer implements TextIndexer {
    private TextTokenizer tokenizerService;
    private IndexStore indexStore;
    private NitriteMap<NitriteId, Document> underlyingMap;
    private final Object lock = new Object();

    NitriteTextIndexer(
            NitriteMap<NitriteId, Document> underlyingMap,
            TextTokenizer textTokenizer,
            IndexStore indexStore) {
        this.tokenizerService = textTokenizer;
        this.indexStore = indexStore;
        this.underlyingMap = underlyingMap;
    }

    @Override
    public void writeIndex(NitriteId id, String field, String text, boolean unique) {
        createOrUpdate(id, field, text);
    }

    @Override
    public void updateIndex(NitriteId id, String field, String newElement, String oldElement, boolean unique) {
        createOrUpdate(id, field, newElement);
    }

    @Override
    public void removeIndex(NitriteId id, String field, String text) {
        try {
            Set<String> words = tokenizerService.tokenize(text);

            synchronized (lock) {
                NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                        = indexStore.getIndexMap(field);

                for (String word : words) {
                    ConcurrentSkipListSet<NitriteId> nitriteIds = indexMap.get(word);
                    if (nitriteIds != null) {
                        nitriteIds.remove(id);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IndexingException(errorMessage(
                    "failed to remove full-text index data for " + field + " with id " + id,
                    IE_REMOVE_FULL_TEXT_INDEX_FAILED));
        }
    }

    @Override
    public void dropIndex(String field) {
        synchronized (lock) {
            indexStore.dropIndex(field);
        }
    }

    @Override
    public void rebuildIndex(String field, boolean unique) {
        for (Map.Entry<NitriteId, Document> entry : underlyingMap.entrySet()) {
            // create the document
            Document object = entry.getValue();

            // retrieve the value from document
            Object fieldValue = getFieldValue(object, field);

            if (fieldValue == null) continue;
            if (!(fieldValue instanceof String)) {
                throw new IndexingException(NON_STRING_VALUE_IN_FULL_TEXT_INDEX);
            }

            writeIndex(entry.getKey(), field, (String) fieldValue, false);
        }
    }

    @Override
    public Set<NitriteId> findText(String field, String searchString) {
        notNull(field, errorMessage("field can not be null", VE_FIND_TEXT_INDEX_NULL_FIELD));
        notNull(searchString, errorMessage("searchString can not be null", VE_FIND_TEXT_INDEX_NULL_VALUE));

        try {
            if (searchString.startsWith("*") || searchString.endsWith("*")) {
                return searchByWildCard(field, searchString);
            } else {
                return searchExactByIndex(field, searchString);
            }
        } catch (IOException ioe) {
            throw new IndexingException(FAILED_TO_QUERY_FTS_DATA, ioe);
        }
    }

    private void createOrUpdate(NitriteId id, String field, String text) {
        try {
            Set<String> words = tokenizerService.tokenize(text);

            synchronized (lock) {
                NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                        = indexStore.getIndexMap(field);

                for (String word : words) {
                    ConcurrentSkipListSet<NitriteId> nitriteIds = indexMap.get(word);

                    if (nitriteIds == null) {
                        nitriteIds = new ConcurrentSkipListSet<>();
                        indexMap.put(word, nitriteIds);
                    }
                    nitriteIds.add(id);
                }
            }
        } catch (IOException ioe) {
            throw new IndexingException(errorMessage(
                    "could not write full-text index data for " + text,
                    IE_FAILED_TO_WRITE_FTS_DATA), ioe);
        }
    }

    private Set<NitriteId> searchByWildCard(String field, String searchString) {
        if (searchString.contentEquals("*")) {
            throw new FilterException(STAR_NOT_A_VALID_SEARCH_STRING);
        }

        StringTokenizer stringTokenizer = new StringTokenizer(searchString);
        if (stringTokenizer.countTokens() > 1) {
            throw new FilterException(MULTIPLE_WORDS_WITH_WILD_CARD);
        }

        if (searchString.startsWith("*") && !searchString.endsWith("*")) {
            return searchByLeadingWildCard(field, searchString);
        } else if (searchString.endsWith("*") && !searchString.startsWith("*")) {
            return searchByTrailingWildCard(field, searchString);
        } else {
            String term = searchString.substring(1, searchString.length() - 1);
            return searchContains(field, term);
        }
    }

    private Set<NitriteId> searchByTrailingWildCard(String field, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException(INVALID_SEARCH_TERM_TRAILING_STAR);
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);
        Set<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(0, searchString.length() - 1);

        for (Map.Entry<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchContains(String field, String term) {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);
        Set<NitriteId> idSet = new LinkedHashSet<>();

        for (Map.Entry<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.contains(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchByLeadingWildCard(String field, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException(INVALID_SEARCH_TERM_LEADING_STAR);
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);
        Set<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(1);

        for (Map.Entry<Comparable, ConcurrentSkipListSet<NitriteId>> entry : indexMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.endsWith(term.toLowerCase())) {
                idSet.addAll(entry.getValue());
            }
        }
        return idSet;
    }

    private Set<NitriteId> searchExactByIndex(String field, String searchString) throws IOException {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        Set<String> words = tokenizerService.tokenize(searchString);
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
}
