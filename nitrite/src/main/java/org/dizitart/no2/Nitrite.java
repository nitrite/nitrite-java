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
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.EntityDecorator;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreMetaData;
import org.dizitart.no2.transaction.Session;

import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryNameByDecorator;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Nitrite is a lightweight, embedded, and self-contained Java NoSQL database.
 * It provides an easy-to-use API to store and retrieve data. Nitrite stores
 * data in the form of documents and supports indexing on fields within
 * the documents to provide efficient search capabilities. Nitrite supports 
 * transactions, and provides a simple and efficient way to persist data.
 *
 * <p>
 * Nitrite is thread-safe and can be used in a multi-threaded environment
 * without any issues. Nitrite is designed to be embedded within the application
 * and does not require any external setup or installation.
 * 
 *
 * @see NitriteBuilder
 * @see NitriteCollection
 * @see ObjectRepository
 * @see EntityDecorator
 * 
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface Nitrite extends AutoCloseable {

    /**
     * Returns a new instance of {@link NitriteBuilder} to build a new Nitrite
     * database instance.
     *
     * @return a new instance of {@link NitriteBuilder}.
     */
    static NitriteBuilder builder() {
        return new NitriteBuilder();
    }

    /**
     * Commits the unsaved changes. For file based store, it saves the changes
     * to disk if there are any unsaved changes.
     * <p>
     * No need to call it after every change, if auto-commit is not disabled
     * while opening the db. However, it may still be called to flush all
     * changes to disk.
     */
    void commit();

    /**
     * Opens a named collection from the store. If the collection does not
     * exist it will be created automatically and returned. If a collection
     * is already opened, it is returned as is. Returned collection is thread-safe
     * for concurrent use.
     *
     * <p>
     * The name cannot contain below reserved strings:
     *
     * <ul>
     * <li>{@link Constants#INTERNAL_NAME_SEPARATOR}</li>
     * <li>{@link Constants#USER_MAP}</li>
     * <li>{@link Constants#INDEX_META_PREFIX}</li>
     * <li>{@link Constants#INDEX_PREFIX}</li>
     * <li>{@link Constants#OBJECT_STORE_NAME_SEPARATOR}</li>
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
     * Opens a type-safe object repository with a key identifier from the store.
     * If the repository does not exist it will be created automatically and
     * returned. If a repository is already opened, it is returned as is.
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
     * Opens a type-safe object repository using a {@link EntityDecorator}. If the
     * repository does not exist it will be created automatically and returned.
     * If a repository is already opened, it is returned as is.
     * <p>
     * The returned repository is thread-safe for concurrent use.
     *
     * @param <T>             the type parameter
     * @param entityDecorator the entityDecorator
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator);

    /**
     * Opens a type-safe object repository using a {@link EntityDecorator} and a key
     * identifier from the store. If the repository does not exist it will be
     * created
     * automatically and returned. If a repository is already opened, it is returned
     * as is.
     * <p>
     * The returned repository is thread-safe for concurrent use.
     *
     * @param <T>             the type parameter
     * @param entityDecorator the entityDecorator
     * @param key             the key
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator, String key);

    /**
     * Destroys a {@link NitriteCollection} without opening it first.
     *
     * @param name the name of the collection
     */
    void destroyCollection(String name);

    /**
     * Destroys an {@link ObjectRepository} without opening it first.
     *
     * @param <T>  the type parameter
     * @param type the type
     */
    <T> void destroyRepository(Class<T> type);

    /**
     * Destroys a keyed-{@link ObjectRepository} without opening it first.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param key  the key
     */
    <T> void destroyRepository(Class<T> type, String key);

    /**
     * Destroys an {@link ObjectRepository} without opening it first.
     *
     * @param <T>  the type parameter
     * @param type the type
     */
    <T> void destroyRepository(EntityDecorator<T> type);

    /**
     * Destroys a keyed-{@link ObjectRepository} without opening it first.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param key  the key
     */
    <T> void destroyRepository(EntityDecorator<T> type, String key);

    /**
     * Gets the set of all {@link NitriteCollection}s' names in the database.
     *
     * @return a set of all collection names in the database
     */
    Set<String> listCollectionNames();

    /**
     * Gets the set of all fully qualified class names corresponding
     * to all {@link ObjectRepository}s in the database.
     *
     * @return a set of all the repository names in the Nitrite database.
     */
    Set<String> listRepositories();

    /**
     * Gets the map of all key to the fully qualified class names corresponding
     * to all keyed-{@link ObjectRepository}s in the store.
     *
     * @return a map of all keyed-repositories keyed by their names
     */
    Map<String, Set<String>> listKeyedRepositories();

    /**
     * Checks if there are any unsaved changes in the Nitrite database.
     *
     * @return {@code true} if there are unsaved changes, {@code false} otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks if the Nitrite database instance is closed.
     *
     * @return {@code true} if the Nitrite database instance is closed;
     *         {@code false} otherwise.
     */
    boolean isClosed();

    /**
     * Gets the {@link NitriteConfig} instance to configure the database.
     *
     * @return the {@link NitriteConfig} instance to configure the database.
     */
    NitriteConfig getConfig();

    /**
     * Returns the {@link NitriteStore} instance associated with this Nitrite
     * database.
     *
     * @return the {@link NitriteStore} instance associated with this Nitrite
     *         database.
     */
    NitriteStore<?> getStore();

    /**
     * Returns the metadata of the database store.
     *
     * @return the metadata of the database store.
     */
    StoreMetaData getDatabaseMetaData();

    /**
     * Creates a new session for the Nitrite database. A session is a lightweight
     * container that holds transactions. Multiple sessions can be created for a
     * single Nitrite database instance.
     *
     * @return a new session for the Nitrite database.
     */
    Session createSession();

    /**
     * Closes the database.
     */
    void close();

    /**
     * Checks if a collection with the given name exists in the database.
     *
     * @param name the name of the collection to check
     * @return true if a collection with the given name exists, false otherwise
     */
    default boolean hasCollection(String name) {
        checkOpened();
        return listCollectionNames().contains(name);
    }

    /**
     * Checks if a repository of the specified type exists in the database.
     *
     * @param type the type of the repository to check for
     * @param <T>  the type of the repository
     * @return true if a repository of the specified type exists, false otherwise
     */
    default <T> boolean hasRepository(Class<T> type) {
        checkOpened();
        String name = findRepositoryName(type, null);
        return listRepositories().contains(name);
    }

    /**
     * Checks if a repository of the specified type and the given key exists in
     * the database.
     *
     * @param type the entity type of the repository
     * @param key  the key of the repository
     * @param <T>  the type of the entity
     * @return true if a repository with the given key exists for the specified
     *         entity type; false otherwise
     */
    default <T> boolean hasRepository(Class<T> type, String key) {
        checkOpened();
        Map<String, Set<String>> keyed = listKeyedRepositories();
        Set<String> entities = keyed.get(key);
        if (entities == null) return false;
        String entityName = ObjectUtils.getEntityName(type);
        return entities.contains(entityName);
    }

    /**
     * Checks if a repository of the specified type described by the
     * {@link EntityDecorator} exists in the database.
     *
     * @param <T>             the type parameter
     * @param entityDecorator entityDecorator
     * @return true if the repository exists; false otherwise.
     */
    default <T> boolean hasRepository(EntityDecorator<T> entityDecorator) {
        checkOpened();
        String name = findRepositoryNameByDecorator(entityDecorator, null);
        return listRepositories().contains(name);
    }

    /**
     * Checks if a keyed-repository of the specified type described by the
     * {@link EntityDecorator} exists in the database.
     *
     * @param <T>             the type parameter.
     * @param entityDecorator entityDecorator.
     * @param key             the key, which will be appended to the repositories
     *                        name.
     * @return true if the repository exists; false otherwise.
     */
    default <T> boolean hasRepository(EntityDecorator<T> entityDecorator, String key) {
        checkOpened();
        Map<String, Set<String>> keyed = listKeyedRepositories();
        Set<String> entities = keyed.get(key);
        if (entities == null) return false;
        return entities.contains(entityDecorator.getEntityName());
    }

    /**
     * Validates the given collection name.
     *
     * @param name the name of the collection to validate
     * @throws ValidationException if the name is null, empty, or contains any
     *                             reserved names
     */
    default void validateCollectionName(String name) {
       notNull(name, "name cannot be null");
       String normalized = name.trim();
       notEmpty(normalized, "name cannot be empty");

        for (String reservedName : RESERVED_NAMES) {
            if (normalized.contains(reservedName)) {
                throw new ValidationException("Name cannot contain " + reservedName);
            }
        }
    }

    /**
     * Checks if the Nitrite database is opened or not. Throws a
     * {@link NitriteIOException} if the database is closed.
     */
    default void checkOpened() {
        if (getStore() == null || getStore().isClosed()) {
            throw new NitriteIOException("Store is closed");
        }
    }
}
