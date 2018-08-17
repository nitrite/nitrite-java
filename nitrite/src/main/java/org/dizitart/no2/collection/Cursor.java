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

package org.dizitart.no2.collection;


import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

/**
 * An interface to iterate over database {@code find()} results. It provides a
 * mechanism to iterate over all {@link NitriteId}s of the result.
 *
 * [[app-listing]]
 * [source,java]
 * . Example of {@link Cursor}
 * --
 *  // create/open a database
 *  Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 *  // create a collection named - test
 *  NitriteCollection collection = db.getCollection("test");
 *
 *  // returns all ids un-filtered
 *  Cursor result = collection.find();
 *
 *  for (Document doc : result) {
 *      // use your logic with the retrieved doc here
 *  }
 *
 *
 * --
 *
 * [icon="{@docRoot}/note.png"]
 * NOTE: To create an iterator over the documents instead of the ids,
 * call on the {@link Cursor}.
 *
 *  @author Anindya Chatterjee
 *  @since 1.0
 */
public interface Cursor extends RecordIterable<Document> {

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    RecordIterable<Document> project(Document projection);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     *
     * It performs an equality match on the localField to the foreignField from the documents of the foreign cursor.
     * If an input document does not contain the localField, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup the lookup parameter for the join operation.
     *
     * @return a lazy iterable of joined documents.
     * @since 2.1.0
     */
    RecordIterable<Document> join(Cursor foreignCursor, Lookup lookup);
}
