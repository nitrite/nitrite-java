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

import org.dizitart.no2.collection.*;
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

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a type-safe persistent java object collection. An object repository
 * is backed by a {@link NitriteCollection}, where all objects are converted
 * into a {@link Document} and saved into the database.
 * <p>
 * An object repository is observable like its underlying {@link NitriteCollection}.
 * </p>
 * <b>Create a repository</b>
 * <pre>
 * {@code
 * // create/open a database
 * Nitrite db = Nitrite.builder()
 *      .openOrCreate("user", "password");
 *
 * // create an object repository
 * ObjectRepository<Employee> employeeStore = db.getRepository(Employee.class);
 *
 * // insert an object
 * Employee emp = new Employee();
 * emp.setName("John Doe");
 * employeeStore.insert(emp);
 * }
 * </pre>
 *
 * @param <T> the type of the object to store.
 * @author Anindya Chatterjee.
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
     * in the repository.
     * <p>
     * If any of the value is already indexed in the repository, then after insertion the
     * index will also be updated.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Insert}.
     * </p>
     *
     * @param object the object to insert
     * @param others other objects to insert in a batch.
     * @return the result of the write operation.
     * @throws ValidationException       if {@code object} is {@code null}.
     * @throws InvalidIdException        if the id value contains {@code null} value.
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

        if (others != null) {
            Collections.addAll(itemList, others);
        }

        return insert(Iterables.toArray(itemList, getType()));
    }

    /**
     * Updates object in the repository. If the filter does not find
     * any object in the collection, then the {@code update} object will be inserted.
     * <p>
     * If the {@code filter} is {@code null}, it will update all objects in the collection.
     * <p>
     * <b>CAUTION:</b>
     * If the {@code update} object has a non {@code null} value in the id value, this value
     * will be removed before update.
     * </p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     * </p>
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} object is {@code null}.
     */
    default WriteResult update(Filter filter, T update) {
        return update(filter, update, updateOptions(false));
    }

    /**
     * Updates object in the repository. Update operation can be customized
     * with the help of {@code updateOptions}.
     * <p>
     * If the {@code filter} is {@code null}, it will update all objects in the collection unless
     * {@code justOnce} is set to {@code true} in {@code updateOptions}.
     * <p>
     * <b>CAUTION:</b>
     * If the {@code update} object has a non {@code null} value in the id value, this value
     * will be removed before update.
     * </p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update} or {@link EventType#Insert}.
     * </p>
     *
     * @param filter         the filter to apply to select objects from the collection.
     * @param update         the modifications to apply.
     * @param updateOptions  various options for update operation.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} object is {@code null}.
     * @throws ValidationException if {@code updateOptions} is {@code null}.
     */
    WriteResult update(Filter filter, T update, UpdateOptions updateOptions);

    /**
     * Updates object in the repository by setting the field specified in {@code document}.
     * <p>
     * If the {@code filter} is {@code null}, it will update all objects in the collection.
     * <p>
     * <b>CAUTION:</b>
     * The {@code update} document should not contain {@code _id} field.
     * </p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     * </p>
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} object is {@code null}.
     */
    default WriteResult update(Filter filter, Document update) {
        return update(filter, update, false);
    }

    /**
     * Updates object in the repository by setting the field specified in {@code document}.
     * Update operation can either update the first matching object or all matching
     * objects depending on the value of {@code justOnce}.
     * <p>
     * If the {@code filter} is {@code null}, it will update all objects in the collection unless
     * {@code justOnce} is set to {@code true}.
     * <p>
     * <b>CAUTION:</b>
     * The {@code update} document should not contain {@code _id} field.
     * </p>
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     * </p>
     * 
     * @param filter   the filter to apply to select objects from the collection.
     * @param update   the modifications to apply.
     * @param justOnce indicates if update should be applied on first matching object or all.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} object is {@code null}.
     */
    WriteResult update(Filter filter, Document update, boolean justOnce);

    /**
     * Removes matching elements from the collection.
     * <p>
     * If the {@code filter} is {@code null}, it will remove all objects from the collection.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     * </p>
     * 
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    default WriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    /**
     * Removes object from the collection. Remove operation can be customized by
     * {@code removeOptions}.
     * <p>
     * If the {@code filter} is {@code null}, it will remove all objects in the collection unless
     * {@code justOnce} is set to {@code true} in {@code removeOptions}.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     * </p>
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
    default Cursor<T> find() {
        return find(Filter.ALL, null);
    }

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected objects.
     * <p>
     * See {@link Filter} for all available filters.
     * <p>
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     * </p>
     *
     * @param filter the filter to apply to select objects from collection.
     * @return a cursor to all selected objects.
     * @see Filter
     * @see Cursor#project(Class)
     */
    default Cursor<T> find(Filter filter) {
        return find(filter, null);
    }

    /**
     * Returns a customized cursor to all objects in the collection.
     *
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected objects.
     */
    default Cursor<T> find(FindOptions findOptions) {
        return find(Filter.ALL, findOptions);
    }

    /**
     * Applies a filter on the collection and returns a customized cursor to the
     * selected objects.
     *
     * <p>
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     * </p>
     *
     * @param filter      the filter to apply to select objects from collection.
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected objects.
     */
    Cursor<T> find(Filter filter, FindOptions findOptions);

    /**
     * Gets a single element from the repository by its id. If no element
     * is found, it will return {@code null}. The object must have a field annotated with {@link Id},
     * otherwise this call will throw {@link InvalidIdException}.
     *
     * @param <I> the type parameter
     * @param id  the id value
     * @return the unique object associated with the id.
     * @throws ValidationException      if <code>id</code> is {@code null}.
     * @throws InvalidIdException       if the id value is {@code null}, or the type is not compatible.
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
     * Returns the underlying {@link NitriteCollection} instance.
     *
     * @return the underlying document collection.
     */
    NitriteCollection getDocumentCollection();
}
