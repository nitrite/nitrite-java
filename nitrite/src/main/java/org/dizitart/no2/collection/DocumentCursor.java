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
import org.dizitart.no2.common.RecordStream;

/**
 * The DocumentCursor represents a cursor as a stream of {@link Document} to iterate over {@link NitriteCollection#find()} results.
 * It also provides methods for projection and perform left outer join with other DocumentCursor.
 * <p>
 * <pre>
 * {@code
 * DocumentCursor result = collection.find();
 *
 * for (Document doc : result) {
 *  // use your logic with the retrieved doc here
 * }
 * }
 * </pre>
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface DocumentCursor extends RecordStream<Document> {
    /**
     * Gets a filter plan for the query.
     *
     * @return the filter plan
     */
    FindPlan getFindPlan();

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    RecordStream<Document> project(Document projection);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     * <p>
     * It performs an equality match on the localField to the foreignField from the documents of the foreign cursor.
     * If an input document does not contain the localField, the join treats the field as having a value of <code>null</code>
     * for matching purposes.
     *
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup        the lookup parameter for the join operation.
     * @return a lazy iterable of joined documents.
     * @since 2.1.0
     */
    RecordStream<Document> join(DocumentCursor foreignCursor, Lookup lookup);
}
