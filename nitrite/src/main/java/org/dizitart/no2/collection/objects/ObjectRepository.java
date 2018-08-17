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

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.event.ChangeAware;
import org.dizitart.no2.event.ChangeType;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.ObjectFilters;
import org.dizitart.no2.index.annotations.Id;

/**
 * Represents a type-safe persistent java object collection. An object repository
 * is backed by a {@link NitriteCollection}, where all objects are converted
 * into a {@link Document} and saved into the database.
 *
 * An object repository is observable like its underlying {@link NitriteCollection}.
 *
 * [[app-listing]]
 * [source,java]
 * .Create a repository
 * --
 * // create/open a database
 *  Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 * // create an object repository
 * ObjectRepository<Employee> employeeStore = db.getRepository(Employee.class);
 *
 * // observe any change to the repository
 * employeeStore.register(new ChangeListener() {
 *
 *      @Override
 *      public void onChange(ChangeInfo changeInfo) {
 *          // your logic based on action
 *      }
 *  });
 *
 * // insert an object
 * Employee emp = new Employee();
 * emp.setName("John Doe");
 * employeeStore.insert(emp);
 *
 * --
 *
 * @param <T> the type of the object to store.
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ChangeAware
 * @see Document
 * @see NitriteId
 * @see org.dizitart.no2.event.ChangeListener
 * @see org.dizitart.no2.event.EventBus
 * @see NitriteCollection
 */
public interface ObjectRepository<T> extends PersistentCollection<T> {

    /**
     * Inserts objects into this repository. If the object contains a value marked with
     * {@link Id}, then the value will be used as an unique key to identify the object
     * in the repository. If the object does not contain any value marked with {@link Id},
     * then nitrite will generate a new {@link NitriteId} and will add it to the document
     * generated from the object.
     *
     * If any of the value is already indexed in the repository, then after insertion the
     * index will also be updated.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#INSERT}.
     *
     * @param object    the object to insert
     * @param others    other objects to insert in a batch.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if `object` is `null`.
     * @throws InvalidIdException if the id value contains `null` value.
     * @throws InvalidIdException if the id value contains non comparable type, i.e.
     * type that does not implement {@link Comparable}.
     * @throws InvalidIdException if the id contains value which is not of the same java
     * type as of other objects' id in the collection.
     * @throws UniqueConstraintException if the value of id value clashes with the id
     * of another object in the collection.
     * @throws UniqueConstraintException if a value of the object is indexed and it
     * violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    @SuppressWarnings("unchecked")
    WriteResult insert(T object, T... others);

    /**
     * Updates objects in the repository. If the filter does not find
     * any object in the collection, then the `update` object will be inserted.
     *
     * If the `filter` is `null`, it will update all objects in the collection.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    WriteResult update(ObjectFilter filter, T update);

    /**
     * Updates objects in the repository. Update operation can be customized
     * with the help of `updateOptions`.
     *
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true` in `updateOptions`.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE} or {@link ChangeType#INSERT}.
     *
     * @param filter        the filter to apply to select objects from the collection.
     * @param update        the modifications to apply.
     * @param upsert        if set to `true`, `update` object will be inserted if not found.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     * @throws org.dizitart.no2.exceptions.ValidationException if `updateOptions` is `null`.
     */
    WriteResult update(ObjectFilter filter, T update, boolean upsert);

    /**
     * Updates objects in the repository by setting the field specified in `document`.
     *
     * If the `filter` is `null`, it will update all objects in the collection.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    WriteResult update(ObjectFilter filter, Document update);

    /**
     * Updates objects in the repository by setting the field specified in `document`.
     * Update operation can either update the first matching object or all matching
     * objects depending on the value of `justOnce`.
     *
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true`.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#UPDATE}.
     *
     * @param filter        the filter to apply to select objects from the collection.
     * @param update        the modifications to apply.
     * @param justOnce      indicates if update should be applied on first matching object or all.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    WriteResult update(ObjectFilter filter, Document update, boolean justOnce);

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
    WriteResult remove(ObjectFilter filter);

    /**
     * Removes objects from the collection. Remove operation can be customized by
     * `removeOptions`.
     *
     * If the `filter` is `null`, it will remove all objects in the collection unless
     * `justOnce` is set to `true` in `removeOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.event.ChangeListener}
     * instances registered to this collection with change type
     * {@link ChangeType#REMOVE}.
     *
     * @param filter the filter to apply to select objects from collection.
     * @param removeOptions the remove options to customize the operations.
     * @return the result of the remove operation.
     */
    WriteResult remove(ObjectFilter filter, RemoveOptions removeOptions);

    /**
     * Returns a cursor to all objects in the collection.
     *
     * @return a cursor to all objects in the collection.
     */
    Cursor<T> find();

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected objects.
     *
     * See {@link ObjectFilters} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter the filter to apply to select objects from collection.
     * @return a cursor to all selected objects.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @see ObjectFilters
     * @see Cursor#project(Class)
     */
    Cursor<T> find(ObjectFilter filter);

    /**
     * Returns a customized cursor to all objects in the collection.
     *
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected objects.
     * @throws org.dizitart.no2.exceptions.ValidationException if `findOptions` is null.
     * @see FindOptions#limit(int, int)
     * @see FindOptions#sort(String, SortOrder)
     * @see SortOrder
     * @see Cursor#project(Class)
     */
    Cursor<T> find(FindOptions findOptions);

    /**
     * Applies a filter on the collection and returns a customized cursor to the
     * selected objects.
     *
     * See {@link ObjectFilters} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter      the filter to apply to select objects from collection.
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected objects.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @throws org.dizitart.no2.exceptions.ValidationException if `findOptions` is null.
     * @see ObjectFilters
     * @see FindOptions#limit(int, int)
     * @see FindOptions#sort(String, SortOrder)
     * @see SortOrder
     * @see Cursor#project(Class)
     */
    Cursor<T> find(ObjectFilter filter, FindOptions findOptions);

    /**
     * Returns the type associated with the {@link ObjectRepository}.
     *
     * @return type of the object.
     * */
    Class<T> getType();

    /**
     * Returns the underlying document collection.
     *
     * @return the underlying document collection.
     * */
    NitriteCollection getDocumentCollection();
}
