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
import org.dizitart.no2.event.ChangeAware;
import org.dizitart.no2.event.ChangeType;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;

/**
 * Represents a named document collection stored in nitrite database.
 * It persists documents into the database. Each document is associated
 * with an unique {@link NitriteId} in a collection.
 *
 * A nitrite collection supports indexing. Every nitrite collection is also
 * observable.
 *
 * [[app-listing]]
 * [source,java]
 * .Create a collection
 * --
 * // create/open a database
 * Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 * include::/src/docs/asciidoc/examples/collection.adoc[]
 *
 * --
 *
 * @see ChangeAware
 * @see Document
 * @see NitriteId
 * @see org.dizitart.no2.event.ChangeListener
 * @see org.dizitart.no2.event.EventBus
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface NitriteCollection extends PersistentCollection<Document> {

    /**
     * Inserts documents into a collection. If the document contains a '_id' value, then
     * the value will be used as an unique key to identify the document in the collection.
     * If the document does not contain any '_id' value, then nitrite will generate a new
     * {@link NitriteId} and will add it to the document.
     *
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#INSERT}.
     *
     * @param document  the document to insert
     * @param documents other documents to insert in a batch.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if `document` is `null`.
     * @throws InvalidIdException if the '_id' value contains `null` value.
     * @throws InvalidIdException if the '_id' value contains non comparable type, i.e.
     * type that does not implement {@link Comparable}.
     * @throws InvalidIdException if the '_id' contains value which is not of the same java
     * type as of other documents' '_id' in the collection.
     * @throws UniqueConstraintException if the value of '_id' value clashes with the id
     * of another document in the collection.
     * @throws UniqueConstraintException if a value of the document is indexed and it
     * violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    WriteResult insert(Document document, Document... documents);

    /**
     * Updates documents in the collection.
     *
     * If the `filter` is `null`, it will update all documents in the collection.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE}.
     *
     * @param filter the filter to apply to select documents from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` document is `null`.
     */
    WriteResult update(Filter filter, Document update);

    /**
     * Updates documents in the collection. Update operation can be customized
     * with the help of `updateOptions`.
     *
     * If the `filter` is `null`, it will update all documents in the collection unless
     * `justOnce` is set to `true` in `updateOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE} or {@link ChangeType#INSERT}.
     *
     * @param filter        the filter to apply to select documents from the collection.
     * @param update        the modifications to apply.
     * @param updateOptions the update options to customize the operation.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` document is `null`.
     * @throws org.dizitart.no2.exceptions.ValidationException if `updateOptions` is `null`.
     * @see UpdateOptions
     */
    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    /**
     * Removes matching elements from the collection.
     *
     * If the `filter` is `null`, it will remove all objects from the collection.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#REMOVE}.
     *
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter);

    /**
     * Removes document from a collection. Remove operation can be customized by
     * `removeOptions`.
     *
     * If the `filter` is `null`, it will remove all documents in the collection unless
     * `justOnce` is set to `true` in `removeOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#REMOVE}.
     *
     * @param filter the filter to apply to select documents from collection.
     * @param removeOptions the remove options to customize the operations.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter, RemoveOptions removeOptions);

    /**
     * Returns a cursor to all documents in the collection.
     *
     * @return a cursor to all documents in the collection.
     */
    Cursor find();

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected documents.
     *
     * See {@link org.dizitart.no2.filters.Filters} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter the filter to apply to select documents from collection.
     * @return a cursor to all selected documents.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @see org.dizitart.no2.filters.Filters
     * @see Cursor#project(Document)
     */
    Cursor find(Filter filter);

    /**
     * Returns a customized cursor to all documents in the collection.
     *
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected documents.
     * @throws org.dizitart.no2.exceptions.ValidationException if `findOptions` is null.
     * @see FindOptions#limit(int, int)
     * @see FindOptions#sort(String, SortOrder)
     * @see SortOrder
     * @see Cursor#project(Document)
     */
    Cursor find(FindOptions findOptions);

    /**
     * Applies a filter on the collection and returns a customized cursor to the
     * selected documents.
     *
     * See {@link org.dizitart.no2.filters.Filters} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter      the filter to apply to select documents from collection.
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected documents.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @throws org.dizitart.no2.exceptions.ValidationException if `findOptions` is null.
     * @see org.dizitart.no2.filters.Filters
     * @see FindOptions#limit(int, int)
     * @see FindOptions#sort(String, SortOrder)
     * @see SortOrder
     * @see Cursor#project(Document)
     */
    Cursor find(Filter filter, FindOptions findOptions);
}
