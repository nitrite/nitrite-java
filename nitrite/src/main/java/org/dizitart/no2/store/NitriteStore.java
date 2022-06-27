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
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.Map;
import java.util.Set;

/**
 * Represents a storage interface for Nitrite database.
 *
 * @param <Config> the type parameter
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface NitriteStore<Config extends StoreConfig> extends NitritePlugin {

    /**
     * Opens or creates this nitrite store.
     */
    void openOrCreate();

    /**
     * Checks whether this store is closed.
     *
     * @return <code>true</code> if closed; <code>false</code> otherwise.
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
     * @return <code>true</code> if here are any changes; <code>false</code> otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether the store is opened in readonly mode.
     *
     * @return <code>true</code> if he store is opened in readonly mode; <code>false</code> otherwise.
     */
    boolean isReadOnly();

    /**
     * Commits the changes. For persistent stores, it also writes
     * changes to disk. It does nothing if there are no unsaved changes.
     */
    void commit();

    /**
     * This method runs before store {@link #close()}, to run cleanup routines.
     */
    void beforeClose();

    /**
     * Checks whether a map with the name already exists in the store or not.
     *
     * @param mapName the map name
     * @return true if the map exists; false otherwise
     */
    boolean hasMap(String mapName);

    /**
     * Opens a {@link NitriteMap} with the default settings. The map is
     * automatically created if it does not yet exist. If a map with this
     * name is already opened, this map is returned.
     *
     * @param <Key>     the key type
     * @param <Value>   the value type
     * @param mapName   the map name
     * @param keyType   the key type
     * @param valueType the value type
     * @return the map.
     */
    <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType);

    /**
     * Closes a {@link NitriteMap} in the store.
     *
     * @param mapName the map name
     */
    void closeMap(String mapName);

    /**
     * Removes a {@link NitriteMap} from the store.
     *
     * @param mapName the map name to remove.
     */
    void removeMap(String mapName);

    /**
     * Opens a {@link NitriteRTree} with the default settings. The RTree is
     * automatically created if it does not yet exist. If a RTree with this
     * name is already open, this RTree is returned.
     *
     * @param <Key>     the key type
     * @param <Value>   the value type
     * @param rTreeName the RTree name
     * @param keyType   the key type
     * @param valueType the value type
     * @return the map.
     */
    <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName, Class<?> keyType, Class<?> valueType);


    /**
     * Closes a RTree in the store.
     *
     * @param rTreeName the RTree name
     */
    void closeRTree(String rTreeName);

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
     * Gets the underlying store engine version.
     *
     * @return the store version
     */
    String getStoreVersion();

    /**
     * Gets the store configuration.
     *
     * @return the store config
     */
    Config getStoreConfig();


    /**
     * Gets the store catalog.
     *
     * @return the catalog
     */
    StoreCatalog getCatalog();
}
