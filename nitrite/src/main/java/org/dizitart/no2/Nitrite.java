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

package org.dizitart.no2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.RepositoryFactory;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.io.Closeable;
import java.nio.channels.NonWritableChannelException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Security.validateUserPassword;
import static org.dizitart.no2.util.ObjectUtils.findObjectStoreName;
import static org.dizitart.no2.util.ValidationUtils.validateCollectionName;


/**
 * = Nitrite
 * 
 * An in-memory, single-file based embedded nosql persistent document store. The store
 * can contains multiple named document collections.
 * 
 * It supports following features:
 * 
 * include::/src/docs/asciidoc/features.adoc[]
 * 
 * [icon="{@docRoot}/note.png"]
 * [NOTE]
 * ====
 *  - It does not support ACID transactions.
 *  - Use {@link NitriteBuilder} to create a db instance.
 * ====
 *
 * @author Anindya Chatterjee
 * @see NitriteBuilder
 * @since 1.0
 */
@Slf4j
public class Nitrite implements Closeable {
    private NitriteStore store;

    /**
     * Provides contextual information for the nitrite database instance.
     * */
    @Getter
    private NitriteContext context;

    Nitrite(NitriteStore store, NitriteContext nitriteContext) {
        this.context = nitriteContext;
        this.store = store;
    }

    /**
     * Provides a builder utility to create a {@link Nitrite} database
     * instance.
     *
     * @return a {@link NitriteBuilder} instance.
     */
    public static NitriteBuilder builder() {
        return new NitriteBuilder();
    }

    /**
     * Opens a named collection from the store. If the collections does not
     * exist it will be created automatically and returned. If a collection
     * is already opened, it is returned as is. Returned collection is thread-safe
     * for concurrent use.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The name can not contain below reserved strings:
     *
     * - {@link Constants#INTERNAL_NAME_SEPARATOR}
     * - {@link Constants#USER_MAP}
     * - {@link Constants#INDEX_META_PREFIX}
     * - {@link Constants#INDEX_PREFIX}
     * - {@link Constants#OBJECT_STORE_NAME_SEPARATOR}
     *
     * ====
     *
     * @param name the name of the collection
     * @return the collection
     * @see NitriteCollection
     */
    public NitriteCollection getCollection(String name) {
        validateCollectionName(name);
        if (store != null) {
            NitriteMap<NitriteId, Document> mapStore = store.openMap(name);
            NitriteCollection collection = CollectionFactory.open(mapStore, context);
            context.getCollectionRegistry().add(name);
            return collection;
        } else {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
        return null;
    }

    /**
     * Opens a type-safe object repository from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     * 
     * [icon="{@docRoot}/note.png"]
     * NOTE: Returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        if (store != null) {
            String name = findObjectStoreName(type);
            NitriteMap<NitriteId, Document> mapStore = store.openMap(name);
            NitriteCollection collection = CollectionFactory.open(mapStore, context);
            ObjectRepository<T> repository = RepositoryFactory.open(type, collection, context);
            context.getRepositoryRegistry().put(name, type);
            return repository;
        } else {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
        return null;
    }

    /**
     * Opens a type-safe object repository with a key identifier from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     * 
     * [icon="{@docRoot}/note.png"]
     * NOTE: Returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter
     * @param key  the key that will be appended to the repositories name
     * @param type the type of the object
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    public <T> ObjectRepository<T> getRepository(String key, Class<T> type) {
        if (store != null) {
            String name = findObjectStoreName(key, type);
            NitriteMap<NitriteId, Document> mapStore = store.openMap(name);
            NitriteCollection collection = CollectionFactory.open(mapStore, context);
            ObjectRepository<T> repository = RepositoryFactory.open(type, collection, context);
            context.getRepositoryRegistry().put(name, type);
            return repository;
        } else {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
        return null;
    }

    /**
     * Gets the set of all {@link NitriteCollection}s' names saved in the store.
     *
     * @return the set of all collections' names.
     */
    public Set<String> listCollectionNames() {
        return new LinkedHashSet<>(context.getCollectionRegistry());
    }

    /**
     * Gets the set of all fully qualified class names corresponding
     * to all {@link ObjectRepository}s in the store.
     *
     * @return the set of all registered classes' names.
     */
    public Set<String> listRepositories() {
        return new LinkedHashSet<>(context.getRepositoryRegistry().keySet());
    }

    /**
     * Checks whether a particular {@link NitriteCollection} exists in the store.
     *
     * @param name the name of the collection.
     * @return `true` if the collection exists; otherwise `false`.
     */
    public boolean hasCollection(String name) {
        return context.getCollectionRegistry().contains(name);
    }

    /**
     * Checks whether a particular {@link ObjectRepository} exists in the store.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return `true` if the repository exists; otherwise `false`.
     */
    public <T> boolean hasRepository(Class<T> type) {
        return context.getRepositoryRegistry().containsKey(findObjectStoreName(type));
    }

    /**
     * Checks whether a particular {@link ObjectRepository} and key combination
     * exists in the store.
     *
     * @param <T>  the type parameter
     * @param key  the key that will be appended to the repositories name
     * @param type the type of the object
     * @return `true` if the repository exists; otherwise `false`.
     */
    public <T> boolean hasRepository(String key, Class<T> type) {
        return context.getRepositoryRegistry().containsKey(findObjectStoreName(key, type));
    }

    /**
     * Checks whether the store has any unsaved changes.
     *
     * @return `true` if there are unsaved changes; otherwise `false`.
     */
    public boolean hasUnsavedChanges() {
        return store != null && store.hasUnsavedChanges();
    }

    /**
     * Compacts store by moving all chunks next to each other.
     */
    public void compact() {
        if (store != null && !store.isClosed()
                && !context.isReadOnly()) {
            store.compact();
            if (log.isDebugEnabled()) {
                log.debug("Store compaction is successful.");
            }
        } else if (store == null) {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
    }

    /**
     * Commits the changes. For file based store, it saves the changes
     * to disk if there are any unsaved changes.
     * 
     * [icon="{@docRoot}/tip.png"]
     * TIP: No need to call it after every change, if auto-commit is not disabled
     * while opening the db. However, it may still be called to flush all
     * changes to disk.
     *
     */
    public void commit() {
        if (store != null && !context.isReadOnly()) {
            store.commit();
            if (log.isDebugEnabled()) {
                log.debug("Unsaved changes committed successfully.");
            }
        } else if (store == null) {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
    }

    /**
     * Closes the database. Unsaved changes are written to disk and compacted first
     * for a file based store.
     */
    public synchronized void close() {
        if (store != null) {
            try {
                if (hasUnsavedChanges()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unsaved changes detected, committing the changes.");
                    }
                    commit();
                }
                if (context.isAutoCompactEnabled()) {
                    compact();
                }

                try {
                    closeCollections();
                    context.shutdown();
                } catch (Throwable error) {
                    log.error("Error while shutting down nitrite.", error);
                }

                store.close();
            } catch (NonWritableChannelException error) {
                if (!context.isReadOnly()) {
                    throw error;
                }
            } finally {
                store = null;
                log.info("Nitrite database has been closed successfully.");
            }
        }
    }

    /**
     * Closes the db immediately without saving last unsaved changes.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operation is called from the JVM shutdown hook to
     * avoid database corruption.
     * */
    synchronized void closeImmediately() {
        if (store != null) {
            try {
                store.closeImmediately();
                context.shutdown();
            } catch (NonWritableChannelException error) {
                if (!context.isReadOnly()) {
                    log.error("Error while closing nitrite store.", error);
                }
            } catch (Throwable t) {
                log.error("Error while closing nitrite store.", t);
            } finally {
                store = null;
                log.info("Nitrite database has been closed by JVM shutdown hook without saving last unsaved changes.");
            }
        }
    }

    /**
     * Checks whether the store is closed.
     *
     * @return `true` if closed; otherwise `false`.
     */
    public boolean isClosed() {
        return store == null || store.isClosed();
    }

    /**
     * Checks if a specific username and password combination is valid to access
     * the database.
     *
     * @param userId   the user id
     * @param password the password
     * @return `true` if valid; otherwise `false`.
     */
    public boolean validateUser(String userId, String password) {
        return validateUserPassword(store, userId, password);
    }

    private void closeCollections() {
        Set<String> collections = context.getCollectionRegistry();
        if (collections != null) {
            for (String name : collections) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            collections.clear();
        }

        Map<String, Class<?>> repositories = context.getRepositoryRegistry();
        if (repositories != null) {
            for (String name : repositories.keySet()) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            repositories.clear();
        }
    }
}
