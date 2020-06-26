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

package org.dizitart.no2.common;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventAware;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.meta.MetadataAware;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.NitriteStore;

import java.io.Closeable;
import java.util.Collection;

/**
 * The interface Persistent collection.
 *
 * @param <T> the type parameter
 * @author Anindya Chatterjee.
 * @see NitriteCollection
 * @see ObjectRepository
 * @since 1.0
 */
public interface PersistentCollection<T> extends EventAware, MetadataAware, Closeable {
    /**
     * Creates an index on `value`, if not already exists.
     * If `indexOptions` is `null`, it will use default options.
     * <p>
     * The default indexing option is -
     * <p>
     * - `indexOptions.setAsync(false);`
     * - `indexOptions.setIndexType(IndexType.Unique);`
     * <p>
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * ====
     * - '_id' value of the document is always indexed. But full text
     * indexing is not supported on '_id' value.
     * - Compound index is not supported.
     * - Indexing on arrays or collection is not supported
     * - Indexing on non-comparable value is not supported
     * ====
     *
     * @param field        the value to be indexed.
     * @param indexOptions index options.
     * @throws org.dizitart.no2.exceptions.IndexingException if an index already exists on `value`.
     * @see org.dizitart.no2.index.IndexOptions
     * @see IndexType
     */
    void createIndex(String field, IndexOptions indexOptions);

    /**
     * Rebuilds index on `field` if it exists.
     *
     * @param field   the value to be indexed.
     * @param isAsync if set to `true`, the indexing will run in background; otherwise, in foreground.
     * @throws org.dizitart.no2.exceptions.IndexingException if the `field` is not indexed.
     */
    void rebuildIndex(String field, boolean isAsync);

    /**
     * Gets a set of all indices in the collection.
     *
     * @return a set of all indices.
     * @see IndexEntry
     */
    Collection<IndexEntry> listIndices();

    /**
     * Checks if a value is already indexed or not.
     *
     * @param field the value to check.
     * @return `true` if the `value` is indexed; otherwise, `false`.
     */
    boolean hasIndex(String field);

    /**
     * Checks if indexing operation is currently ongoing for a `field`.
     *
     * @param field the value to check.
     * @return `true` if indexing is currently running; otherwise, `false`.
     */
    boolean isIndexing(String field);

    /**
     * Drops the index on a `field`.
     *
     * @param field the index of the `field` to drop.
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is currently running on the `field`.
     * @throws org.dizitart.no2.exceptions.IndexingException if the `field` is not indexed.
     */
    void dropIndex(String field);

    /**
     * Drops all indices from the collection.
     *
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is running on any value.
     */
    void dropAllIndices();

    /**
     * Inserts elements into this collection. If the element has an '_id' field,
     * then the value will be used as an unique key to identify the element
     * in the collection. If the element does not have any '_id' field,
     * then nitrite will generate a new {@link org.dizitart.no2.collection.NitriteId} and will add it to the '_id'
     * field.
     * <p>
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Insert}.
     *
     * @param elements an array of element for batch insertion.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException       if `elements` is `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field's value contains `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field's value contains non comparable type, i.e. type that does not implement {@link Comparable}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field contains value which is not of the same java type as of other element's '_id' field value in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if the value of '_id' field clashes with the '_id' field of another element in the repository.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if a value of the element is indexed and it violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    WriteResult insert(T[] elements);

    /**
     * Updates `element` in the collection. Specified `element` must have an id.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     *
     * @param element the element to update.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if the `element` is `null`.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element` does not have any id.
     */
    default WriteResult update(T element) {
        return update(element, false);
    }

    /**
     * Updates `element` in the collection. Specified `element` must have an id.
     * If the `element` is not found in the collection, it will be inserted only if `upsert`
     * is set to `true`.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update
     * or {@link EventType#Insert}.
     *
     * @param element        the element to update.
     * @param insertIfAbsent if set to `true`, `element` will be inserted if not found.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if the `element` is `null`.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element`
     *                                                              does not have any id field.
     */
    WriteResult update(T element, boolean insertIfAbsent);

    /**
     * Deletes the `element` from the collection. The `element` must have an id.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     *
     * @param element the element
     * @return the result of the remove operation.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element` does not
     *                                                              have any id field.
     */
    WriteResult remove(T element);

    /**
     * Drops the collection and all of its indices.
     * <p>
     * Any further access to a dropped collection would result into
     * a {@link IllegalStateException}.
     * <p>
     */
    void drop();

    /**
     * Returns `true` if the collection is dropped; otherwise, `false`.
     *
     * @return a boolean value indicating if the collection has been dropped or not.
     */
    boolean isDropped();

    /**
     * Returns `true` if the collection is open; otherwise, `false`.
     *
     * @return a boolean value indicating if the collection has been closed or not.
     */
    boolean isOpen();

    /**
     * Closes the collection for further access. If a collection once closed
     * can only be opened via {@link org.dizitart.no2.Nitrite#getCollection(String)} or
     * {@link org.dizitart.no2.Nitrite#getRepository(Class)} operation.
     * <p>
     * Any access to a closed collection would result into a {@link IllegalStateException}.
     */
    void close();

    /**
     * Returns the size of the {@link PersistentCollection}.
     *
     * @return the size.
     */
    long size();

    /**
     * Returns the {@link NitriteStore} instance for this collection.
     *
     * @return the {@link NitriteStore} instance.
     */
    NitriteStore getStore();
}
