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

package org.dizitart.no2.store;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.module.NitritePlugin;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.Map;
import java.util.Set;

/**
 * Represents a persistent storage for Nitrite database.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface NitriteStore extends NitritePlugin, AutoCloseable {

    void openOrCreate(String username, String password, StoreConfig storeConfig);

    /**
     * Checks whether this store is closed for further modification.
     *
     * @return `true` if closed; `false` otherwise.
     */
    boolean isClosed();

    /**
     * Gets the set of all {@link NitriteCollection} names in store.
     *
     * @return the set of names.
     */
    Set<String> getCollectionNames();

    /**
     * Gets the set of all {@link ObjectRepository} details in store.
     *
     * @return the details of all {@link ObjectRepository}.
     */
    Set<String> getRepositoryRegistry();

    /**
     * Gets the set of all keyed-{@link ObjectRepository} details in store.
     *
     * @return the details of all {@link ObjectRepository}.
     */
    Map<String, Set<String>> getKeyedRepositoryRegistry();

    /**
     * Checks whether there are any unsaved changes.
     *
     * @return `true` if there are any changes; `false` otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether the store is opened in readonly mode.
     *
     * @return `true` if the store is opened in readonly mode.; `false` otherwise.
     */
    boolean isReadOnly();

    /**
     * Commits the changes. For persistent stores, it also writes
     * changes to disk. It does nothing if there are no unsaved changes.
     */
    void commit();

    /**
     * Closes the file and the store. Unsaved changes are written to disk first.
     */
    void close();

    /**
     * This method runs before {@link #close()}, to run cleanup routines.
     */
    void beforeClose();

    /**
     * Gets the {@link IndexCatalog} instances from the store.
     *
     * @return the IndexCatalog instance.
     */
    IndexCatalog getIndexCatalog();

    /**
     * Opens a {@link NitriteMap} with the default settings. The map is
     * automatically create if it does not yet exist. If a map with this
     * name is already open, this map is returned.
     *
     * @param <Key>   the key type
     * @param <Value> the value type
     * @param mapName the map name
     * @return the map.
     */
    <Key, Value> NitriteMap<Key, Value> openMap(String mapName);

    /**
     * Removes a map from the store.
     *
     * @param mapName the map name to remove.
     */
    void removeMap(String mapName);

    /**
     * Opens a {@link NitriteRTree} with the default settings. The RTree is
     * automatically create if it does not yet exist. If a RTree with this
     * name is already open, this RTree is returned.
     *
     * @param <Key>     the key type
     * @param <Value>   the value type
     * @param rTreeName the RTree name
     * @return the map.
     */
    <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName);

    /**
     * Removes a RTree from the store.
     *
     * @param rTreeName the RTree name to remove.
     */
    void removeRTree(String rTreeName);

    /**
     * Adds a {@link StoreEventListener} to listen to all store events.
     *
     * @param listener the listener instances.
     */
    void subscribe(StoreEventListener listener);

    /**
     * Removes a {@link StoreEventListener} to unsubscribe from all store events.
     *
     * @param listener the listener instances.
     */
    void unsubscribe(StoreEventListener listener);

    /**
     * Returns information about the underlying data store.
     *
     * @return a {@link StoreInfo} object containing information about the store.
     */
    StoreInfo getStoreInfo();
}
