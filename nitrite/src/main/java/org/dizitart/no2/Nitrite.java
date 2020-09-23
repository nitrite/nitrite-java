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

package org.dizitart.no2;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.DatabaseMetaData;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.transaction.Session;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * An in-memory, single-file based embedded nosql persistent document store. The store
 * can contains multiple named document collections.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface Nitrite extends Closeable {

    static NitriteBuilder builder() {
        return new NitriteBuilder();
    }

    /**
     * Closes the database. Unsaved changes are written to disk for a file based store.
     */
    void close();

    /**
     * Commits the changes. For file based store, it saves the changes
     * to disk if there are any unsaved changes.
     * <p>
     * No need to call it after every change, if auto-commit is not disabled
     * while opening the db. However, it may still be called to flush all
     * changes to disk.
     */
    void commit();

    /**
     * Opens a named collection from the store. If the collections does not
     * exist it will be created automatically and returned. If a collection
     * is already opened, it is returned as is. Returned collection is thread-safe
     * for concurrent use.
     *
     * <p>
     * The name cannot contain below reserved strings:
     *
     * <ul>
     *     <li>{@link Constants#INTERNAL_NAME_SEPARATOR}</li>
     *     <li>{@link Constants#USER_MAP}</li>
     *     <li>{@link Constants#INDEX_META_PREFIX}</li>
     *     <li>{@link Constants#INDEX_PREFIX}</li>
     *     <li>{@link Constants#OBJECT_STORE_NAME_SEPARATOR}</li>
     * </ul>
     *
     * @param name the name of the collection
     * @return the collection
     * @see NitriteCollection
     */
    NitriteCollection getCollection(String name);

    /**
     * Opens a type-safe object repository from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     * <p>
     * The returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type);

    /**
     * Opens a type-safe object repository with a key identifier from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     * <p>
     * The returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter.
     * @param type the type of the object.
     * @param key  the key, which will be appended to the repositories name.
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type, String key);

    /**
     * Gets the set of all {@link NitriteCollection}s' names saved in the store.
     *
     * @return the set of all collections' names.
     */
    Set<String> listCollectionNames();

    /**
     * Gets the set of all fully qualified class names corresponding
     * to all {@link ObjectRepository}s in the store.
     *
     * @return the set of all registered classes' names.
     */
    Set<String> listRepositories();

    /**
     * Gets the map of all key to the fully qualified class names corresponding
     * to all keyed-{@link ObjectRepository}s in the store.
     *
     * @return the set of all registered classes' names.
     */
    Map<String, Set<String>> listKeyedRepository();

    /**
     * Checks whether the store has any unsaved changes.
     *
     * @return `true` if there are unsaved changes; otherwise `false`.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether the store is closed.
     *
     * @return `true` if closed; otherwise `false`.
     */
    boolean isClosed();

    /**
     * Gets the {@link NitriteConfig} instance to configure the database.
     *
     * @return the {@link NitriteConfig} instance to configure the database.
     */
    NitriteConfig getConfig();

    /**
     * Gets the {@link NitriteStore} instance powering the database.
     *
     * @return the {@link NitriteStore} instance of the database.
     */
    NitriteStore<?> getStore();

    /**
     * Gets database meta data.
     *
     * @return the database meta data
     */
    DatabaseMetaData getDatabaseMetaData();


    /**
     * Creates a {@link Session} for transaction.
     *
     * @return the session
     */
    Session createSession();

    /**
     * Checks whether a particular {@link NitriteCollection} exists in the store.
     *
     * @param name the name of the collection.
     * @return `true` if the collection exists; otherwise `false`.
     */
    default boolean hasCollection(String name) {
        checkOpened();
        return listCollectionNames().contains(name);
    }

    /**
     * Checks whether a particular {@link ObjectRepository} exists in the store.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return `true` if the repository exists; otherwise `false`.
     */
    default <T> boolean hasRepository(Class<T> type) {
        checkOpened();
        return listRepositories().contains(type.getName());
    }

    /**
     * Checks whether a particular keyed-{@link ObjectRepository} exists in the store.
     *
     * @param <T>  the type parameter.
     * @param key  the key, which will be appended to the repositories name.
     * @param type the type of the object.
     * @return `true` if the repository exists; otherwise `false`.
     */
    default <T> boolean hasRepository(Class<T> type, String key) {
        checkOpened();
        return listKeyedRepository().containsKey(key)
            && listKeyedRepository().get(key).contains(type.getName());
    }

    default void validateCollectionName(String name) {
        notNull(name, "name cannot be null");
        notEmpty(name, "name cannot be empty");

        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) {
                throw new ValidationException("name cannot contain " + reservedName);
            }
        }
    }

    default void checkOpened() {
        if (getStore() == null || getStore().isClosed()) {
            throw new NitriteIOException("store is closed");
        }
    }
}
