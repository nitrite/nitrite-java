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

package org.dizitart.no2.collection;

import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;

import java.text.Collator;

/**
 * An interface to iterate over database {@code find()} results. It provides a
 * mechanism to iterate over all {@link NitriteId}s of the result.
 * <p>
 * [[app-listing]]
 * [source,java]
 * .Example of {@link DocumentCursor}
 * --
 * // create/open a database
 * Nitrite db = Nitrite.builder()
 * .openOrCreate("user", "password");
 * <p>
 * // create a collection named - test
 * NitriteCollection collection = db.getCollection("test");
 * <p>
 * // returns all ids un-filtered
 * DocumentCursor result = collection.find();
 * <p>
 * for (Document doc : result) {
 * // use your logic with the retrieved doc here
 * }
 * <p>
 * <p>
 * --
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface DocumentCursor extends ReadableStream<Document> {

    DocumentCursor sort(String field, SortOrder sortOrder, Collator collator, NullOrder nullOrder);

    DocumentCursor skipLimit(long skip, long size);

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    ReadableStream<Document> project(Document projection);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     * <p>
     * It performs an equality match on the localString to the foreignString from the documents of the foreign cursor.
     * If an input document does not contain the localString, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup        the lookup parameter for the join operation.
     * @return a lazy iterable of joined documents.
     * @since 2.1.0
     */
    ReadableStream<Document> join(DocumentCursor foreignCursor, Lookup lookup);

    default DocumentCursor skip(long skip) {
        return skipLimit(skip, size());
    }

    default DocumentCursor limit(long limit) {
        return skipLimit(0, limit);
    }

    default DocumentCursor sort(String field) {
        return sort(field, SortOrder.Ascending);
    }

    default DocumentCursor sort(String field, SortOrder sortOrder) {
        return sort(field, sortOrder, NullOrder.Default);
    }

    default DocumentCursor sort(String field, SortOrder sortOrder, Collator collator) {
        return sort(field, sortOrder, collator, NullOrder.Default);
    }

    default DocumentCursor sort(String field, SortOrder sortOrder, NullOrder nullOrder) {
        return sort(field, sortOrder, null, nullOrder);
    }
}
