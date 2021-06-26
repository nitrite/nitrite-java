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
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.NitriteStore;

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
public interface PersistentCollection<T> extends EventAware, MetadataAware, AutoCloseable {

    /**
     * Adds a data processor to this collection.
     *
     * @param processor the processor
     */
    void addProcessor(Processor processor);

    /**
     * Removes a data processor from this collection.
     *
     * @param processor the processor
     */
    void removeProcessor(Processor processor);

    /**
     * Creates an unique index on the {@code fields}, if not already exists.
     *
     * @param fields       the fields to be indexed.
     * @throws org.dizitart.no2.exceptions.IndexingException if an index already exists on the field.
     */
    default void createIndex(String... fields) {
        createIndex(null, fields);
    }

    /**
     * Creates an index on the {@code fields}, if not already exists.
     * If {@code indexOptions} is {@code null}, it will use default options.
     * <p>
     * <p>
     * The default indexing option is -
     *
     * <ul>
     * <li>{@code indexOptions.setIndexType(IndexType.Unique);}</li>
     * </ul>
     *
     * <p>
     *     NOTE:
     *     <ul>
     *         <li><b>_id</b> value of the document is always indexed. But full-text indexing is not supported on <b>_id</b> value.</li>
     *         <li>Indexing on non-comparable value is not supported.</li>
     *     </ul>
     * </p>
     *
     * @param indexOptions index options.
     * @param fields       the fields to be indexed.
     * @throws org.dizitart.no2.exceptions.IndexingException if an index already exists on the field.
     * @see org.dizitart.no2.index.IndexOptions
     * @see IndexType
     */
    void createIndex(IndexOptions indexOptions, String... fields);

    /**
     * Rebuilds index on the {@code field} if it exists.
     *
     * @param fields the fields to be indexed.
     * @throws org.dizitart.no2.exceptions.IndexingException if the {@code field} is not indexed.
     */
    void rebuildIndex(String... fields);


    /**
     * Gets a set of all indices in the collection.
     *
     * @return a set of all indices.
     * @see IndexDescriptor
     */
    Collection<IndexDescriptor> listIndices();

    /**
     * Checks if the {@code fields} is already indexed or not.
     *
     * @param fields the fields to check.
     * @return {@code true} if the {@code field} is indexed; otherwise, {@code false}.
     */
    boolean hasIndex(String... fields);

    /**
     * Checks if indexing operation is currently ongoing for the {@code fields}.
     *
     * @param fields the fields to check.
     * @return {@code true} if indexing is currently running; otherwise, {@code false}.
     */
    boolean isIndexing(String... fields);

    /**
     * Drops the index on the {@code fields}.
     *
     * @param fields the index on the {@code fields} to drop.
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is currently running on the {@code fields}.
     * @throws org.dizitart.no2.exceptions.IndexingException if the {@code fields} are not indexed.
     */
    void dropIndex(String... fields);

    /**
     * Drops all indices from the collection.
     *
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is running on any value.
     */
    void dropAllIndices();

    /**
     * Inserts elements into this collection. If the element has an <b>_id</b> field,
     * then the value will be used as an unique key to identify the element
     * in the collection. If the element does not have any <b>_id</b> field,
     * then nitrite will generate a new {@link org.dizitart.no2.collection.NitriteId} and will add it to the <b>_id</b>
     * field.
     * <p>
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     * <p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type {@link EventType#Insert}.
     * </p>
     *
     * @param elements an array of element for batch insertion.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException       if elements is null.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the <b>_id</b> field's value contains {@code null}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the <b>_id</b> field's value contains non comparable type, i.e. type that does not implement {@link Comparable}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the <b>_id</b> field contains value which is not of the same java type as of other element's <b>_id</b> field value in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if the value of <b>_id</b> field clashes with the <b>_id</b> field of another element in the repository.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if a value of the element is indexed and it violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    WriteResult insert(T[] elements);

    /**
     * Updates the {@code element} in the collection. Specified {@code element} must have an id.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     * </p>
     *
     * @param element the element to update.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if the element is {@code null}.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the element does not have any id.
     */
    default WriteResult update(T element) {
        return update(element, false);
    }

    /**
     * Updates {@code element} in the collection. Specified {@code element} must have an id.
     * If the {@code element} is not found in the collection, it will be inserted only if {@code insertIfAbsent}
     * is set to {@code true}.
     * <p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update} or {@link EventType#Insert}.
     * </p>
     *
     * @param element        the element to update.
     * @param insertIfAbsent if set to {@code true}, {@code element} will be inserted if not found.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if the {@code element} is {@code null}.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the {@code element} does not have any id field.
     */
    WriteResult update(T element, boolean insertIfAbsent);

    /**
     * Deletes the {@code element} from the collection. The {@code element} must have an id.
     *
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     * </p>
     *
     * @param element the element
     * @return the result of the remove operation.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the {@code element} does not have any id field.
     */
    WriteResult remove(T element);

    /**
     * Removes all element from the collection.
     */
    void clear();

    /**
     * Drops the collection and all of its indices.
     * <p>
     * Any further access to a dropped collection would result into
     * a {@link IllegalStateException}.
     * </p>
     */
    void drop();

    /**
     * Returns {@code true} if the collection is dropped; otherwise, {@code false}.
     *
     * @return a boolean value indicating if the collection has been dropped or not.
     */
    boolean isDropped();

    /**
     * Returns {@code true} if the collection is open; otherwise, {@code false}.
     *
     * @return a boolean value indicating if the collection has been closed or not.
     */
    boolean isOpen();

    /**
     * Returns the size of the {@link PersistentCollection}.
     *
     * @return the size.
     */
    long size();

    /**
     * Closes this {@link PersistentCollection}.
     * */
    void close();

    /**
     * Returns the {@link NitriteStore} instance for this collection.
     *
     * @return the {@link NitriteStore} instance.
     */
    NitriteStore<?> getStore();
}
