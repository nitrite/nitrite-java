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

package org.dizitart.no2.repository;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventAware;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.PersistentCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.annotations.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a type-safe persistent java object collection. An object repository
 * is backed by a {@link NitriteCollection}, where all objects are converted
 * into a {@link Document} and saved into the database.
 * <p>
 * An object repository is observable like its underlying {@link NitriteCollection}.
 * <p>
 * [[app-listing]]
 * [source,java]
 * .Create a repository
 * --
 * // create/open a database
 * Nitrite db = Nitrite.builder()
 * .openOrCreate("user", "password");
 * <p>
 * // create an object repository
 * ObjectRepository<Employee> employeeStore = db.getRepository(Employee.class);
 * <p>
 * // observe any change to the repository
 * employeeStore.register(new ChangeListener() {
 *
 * @param <T> the type of the object to store.
 * @author Anindya Chatterjee.
 * @Override public void onChange(ChangeInfo changeInfo) {
 * // your logic based on action
 * }
 * });
 * <p>
 * // insert an object
 * Employee emp = new Employee();
 * emp.setName("John Doe");
 * employeeStore.insert(emp);
 * <p>
 * --
 * @see EventAware
 * @see Document
 * @see NitriteId
 * @see CollectionEventListener
 * @see org.dizitart.no2.common.event.EventBus
 * @see NitriteCollection
 * @since 1.0
 */
public interface ObjectRepository<T> extends PersistentCollection<T> {
    /**
     * Inserts object into this repository. If the object contains a value marked with
     * {@link Id}, then the value will be used as a unique key to identify the object
     * in the repository. If the object does not contain any value marked with {@link Id},
     * then nitrite will generate a new {@link NitriteId} and will add it to the document
     * generated from the object.
     * <p>
     * If any of the value is already indexed in the repository, then after insertion the
     * index will also be updated.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Insert}.
     *
     * @param object the object to insert
     * @param others other objects to insert in a batch.
     * @return the result of the write operation.
     * @throws ValidationException       if `object` is `null`.
     * @throws InvalidIdException        if the id value contains `null` value.
     * @throws InvalidIdException        if the id value contains non comparable type, i.e. type that does not implement {@link Comparable}.
     * @throws InvalidIdException        if the id contains value which is not of the same java type as of other objects' id in the collection.
     * @throws UniqueConstraintException if the value of id value clashes with the id of another object in the collection.
     * @throws UniqueConstraintException if a value of the object is indexed, and it violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    @SuppressWarnings("unchecked")
    default WriteResult insert(T object, T... others) {
        notNull(object, "a null object cannot be inserted");
        if (others != null) {
            containsNull(others, "a null object cannot be inserted");
        }

        List<T> itemList = new ArrayList<>();
        itemList.add(object);

        if (others != null && itemList.size() > 0) {
            Collections.addAll(itemList, others);
        }

        return insert(Iterables.toArray(itemList, getType()));
    }

    /**
     * Updates object in the repository. If the filter does not find
     * any object in the collection, then the `update` object will be inserted.
     * <p>
     * If the `filter` is `null`, it will update all objects in the collection.
     * <p>
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws ValidationException if the `update` object is `null`.
     */
    default WriteResult update(Filter filter, T update) {
        return update(filter, update, false);
    }

    /**
     * Updates object in the repository. Update operation can be customized
     * with the help of `updateOptions`.
     * <p>
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true` in `updateOptions`.
     * <p>
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update} or
     * {@link EventType#Insert}.
     *
     * @param filter         the filter to apply to select objects from the collection.
     * @param update         the modifications to apply.
     * @param insertIfAbsent if set to `true`, `update` object will be inserted if not found.
     * @return the result of the update operation.
     * @throws ValidationException if the `update` object is `null`.
     * @throws ValidationException if `updateOptions` is `null`.
     */
    WriteResult update(Filter filter, T update, boolean insertIfAbsent);

    /**
     * Updates object in the repository by setting the field specified in `document`.
     * <p>
     * If the `filter` is `null`, it will update all objects in the collection.
     * <p>
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws ValidationException if the `update` object is `null`.
     */
    default WriteResult update(Filter filter, Document update) {
        return update(filter, update, false);
    }

    /**
     * Updates object in the repository by setting the field specified in `document`.
     * Update operation can either update the first matching object or all matching
     * objects depending on the value of `justOnce`.
     * <p>
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true`.
     * <p>
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     *
     * @param filter   the filter to apply to select objects from the collection.
     * @param update   the modifications to apply.
     * @param justOnce indicates if update should be applied on first matching object or all.
     * @return the result of the update operation.
     * @throws ValidationException if the `update` object is `null`.
     */
    WriteResult update(Filter filter, Document update, boolean justOnce);

    /**
     * Removes matching elements from the collection.
     * <p>
     * If the `filter` is `null`, it will remove all objects from the collection.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     *
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    default WriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    /**
     * Removes object from the collection. Remove operation can be customized by
     * `removeOptions`.
     * <p>
     * If the `filter` is `null`, it will remove all objects in the collection unless
     * `justOnce` is set to `true` in `removeOptions`.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     *
     * @param filter  the filter to apply to select objects from collection.
     * @param justOne indicates if only one element will be removed or all of them.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter, boolean justOne);

    /**
     * Returns a cursor to all objects in the collection.
     *
     * @return a cursor to all objects in the collection.
     */
    Cursor<T> find();

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected objects.
     * <p>
     * See {@link Filter} for all available filters.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter the filter to apply to select objects from collection.
     * @return a cursor to all selected objects.
     * @throws ValidationException if `filter` is null.
     * @see Filter
     * @see Cursor#project(Class)
     */
    Cursor<T> find(Filter filter);

    /**
     * Gets a single element from the repository by its id. If no element
     * is found, it will return `null`. The object must have a field annotated with {@link Id},
     * otherwise this call will throw {@link InvalidIdException}.
     *
     * @param id the id value
     * @return the unique object associated with the id.
     * @throws ValidationException      if `id` is `null`.
     * @throws InvalidIdException       if the id value is `null`, or the type is not compatible.
     * @throws NotIdentifiableException if the object has no field marked with {@link Id}.
     */
    <I> T getById(I id);

    /**
     * Returns the type associated with the {@link ObjectRepository}.
     *
     * @return type of the object.
     */
    Class<T> getType();

    /**
     * Returns the underlying document collection.
     *
     * @return the underlying document collection.
     */
    NitriteCollection getDocumentCollection();
}
