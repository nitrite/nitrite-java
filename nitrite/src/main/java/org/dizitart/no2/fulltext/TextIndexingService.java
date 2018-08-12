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

package org.dizitart.no2.fulltext;

import org.dizitart.no2.NitriteId;

import java.util.Set;

/**
 * Represents a full-text indexing engine. It scans a document
 * and modifies full-text index entries by decomposing texts of
 * an indexed field, into a set of string tokens. It uses the
 * full-text index to search for a specific text.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see org.dizitart.no2.NitriteBuilder#textIndexingService(TextIndexingService)
 * @see org.dizitart.no2.filters.Filters#text(String, String)
 * @see org.dizitart.no2.objects.filters.ObjectFilters#text(String, String)
 */
public interface TextIndexingService {
    /**
     * Creates a full-text index entry from a specific `field`
     * containing the `text`.
     *
     * @param id    the id associated with the document containing the `text`
     * @param field the indexed field
     * @param text  the text to index
     */
    void createIndex(NitriteId id, String field, String text);

    /**
     * Updates a full-text index entry.
     *
     * @param id    the id associated with the document containing the `text`
     * @param field the indexed field
     * @param text  the text to update
     */
    void updateIndex(NitriteId id, String field, String text);

    /**
     * Deletes a full-text index entry.
     *
     * @param id    the id associated with the document containing the `text`
     * @param field the indexed field
     * @param text  the text to delete
     */
    void deleteIndex(NitriteId id, String field, String text);

    /**
     * Deletes all full-text index entries of the `field`.
     *
     * @param field the field
     */
    void deleteIndexesByField(String field);

    /**
     * Searches `searchString` using the full-text index of the `field`.
     *
     * @param field        the field
     * @param searchString the search string to search using index
     * @return the set of {@link NitriteId}s for the matching documents.
     */
    Set<NitriteId> searchByIndex(String field, String searchString);

    /**
     * Drops all full-text index entries of a collection.
     */
    void drop();

    /**
     * Deletes all full-text index entries.
     */
    void clear();
}
