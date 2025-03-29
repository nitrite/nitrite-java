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

package org.dizitart.no2.filters;

import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexMap;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.common.util.StringUtils.stringTokenizer;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Setter
public class TextFilter extends StringFilter {
    private TextTokenizer textTokenizer;

    /**
     * Instantiates a new Text filter.
     *
     * @param field the field
     * @param value the value
     */
    TextFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean applyOnString(String value) {
        String searchString = (String) this.getValue();
        if (searchString.startsWith("*") || searchString.endsWith("*")) {
            searchString = searchString.replace("*", "");
        }
        return value.toLowerCase().contains(searchString.toLowerCase());
    }

    /**
     * Applies this filter to a document, checking if the specified field contains the search string.
     *
     * @param element the document pair to filter
     * @return true if the field value contains the search string (case-insensitive), false otherwise
     * @throws FilterException if the field is not a string
     */
    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        notNull(getField(), "field cannot be null");
        notNull(getStringValue(), "search term cannot be null");
        Object docValue = element.getSecond().get(getField());

        if (!(docValue instanceof String)) {
            throw new FilterException("Text filter can not be applied on non string field " + getField());
        }

        String docString = (String) docValue;
        return applyOnString(docString);
    }

    @Override
    public String toString() {
        return "(" + getField() + " like " + getValue() + ")";
    }

    /**
     * Apply this filter on text index.
     *
     * @param indexMap the index map
     * @return the linked hash set
     */
    public LinkedHashSet<NitriteId> applyOnTextIndex(NitriteMap<String, List<?>> indexMap) {
        notNull(getField(), "field cannot be null");
        notNull(getStringValue(), "search term cannot be null");
        String searchString = getStringValue();

        if (searchString.startsWith("*") || searchString.endsWith("*")) {
            return searchByWildCard(indexMap, searchString);
        } else {
            return searchExactByIndex(indexMap, searchString);
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> searchExactByIndex(NitriteMap<String, List<?>> indexMap, String searchString) {

        Set<String> words = textTokenizer.tokenize(searchString);
        Map<NitriteId, Integer> scoreMap = new HashMap<>();
        for (String word : words) {
            List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(word);
            if (nitriteIds != null) {
                for (NitriteId id : nitriteIds) {
                    scoreMap.merge(id, 1, Integer::sum);
                }
            }
        }

        return sortedIdsByScore(scoreMap);
    }

    private LinkedHashSet<NitriteId> searchByWildCard(NitriteMap<String, List<?>> indexMap, String searchString) {
        if (searchString.contentEquals("*")) {
            throw new FilterException("* is not a valid search term");
        }

        StringTokenizer stringTokenizer = stringTokenizer(searchString);
        if (stringTokenizer.countTokens() > 1) {
            throw new FilterException("Wild card search can not be applied on " +
                "multiple words");
        }

        if (searchString.startsWith("*") && !searchString.endsWith("*")) {
            return searchByLeadingWildCard(indexMap, searchString);
        } else if (searchString.endsWith("*") && !searchString.startsWith("*")) {
            return searchByTrailingWildCard(indexMap, searchString);
        } else {
            String term = searchString.substring(1, searchString.length() - 1);
            return searchContains(indexMap, term);
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> searchByLeadingWildCard(NitriteMap<String, List<?>> indexMap, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("* is not a valid search term");
        }

        LinkedHashSet<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(1);

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.endsWith(term.toLowerCase())) {
                idSet.addAll((List<NitriteId>) entry.getSecond());
            }
        }
        return idSet;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> searchByTrailingWildCard(NitriteMap<String, List<?>> indexMap, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("* is not a valid search term");
        }

        LinkedHashSet<NitriteId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(0, searchString.length() - 1);

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.startsWith(term.toLowerCase())) {
                idSet.addAll((List<NitriteId>) entry.getSecond());
            }
        }
        return idSet;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> searchContains(NitriteMap<String, List<?>> indexMap, String term) {
        LinkedHashSet<NitriteId> idSet = new LinkedHashSet<>();

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.contains(term.toLowerCase())) {
                idSet.addAll((List<NitriteId>) entry.getSecond());
            }
        }
        return idSet;
    }

    private LinkedHashSet<NitriteId> sortedIdsByScore(Map<NitriteId, Integer> unsortedMap) {
        List<Map.Entry<NitriteId, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort((e1, e2) -> (e2.getValue()).compareTo(e1.getValue()));

        LinkedHashSet<NitriteId> result = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Integer> entry : list) {
            result.add(entry.getKey());
        }

        return result;
    }

    @Override
    public List<?> applyOnIndex(IndexMap indexMap) {
        return null;
    }

}
