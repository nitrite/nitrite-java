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
     * Opens the store if it exists, or creates a new one if it doesn't.
     */
    void openOrCreate();

    /**
     * Checks whether this store is closed.
     *
     * @return <code>true</code> if closed; <code>false</code> otherwise.
     */
    boolean isClosed();

    /**
     * Returns a set of all collection names in the store.
     *
     * @return a set of all collection names in the store
     */
    Set<String> getCollectionNames();

    /**
     * Returns a set of all the repository names registered in the Nitrite store.
     *
     * @return a set of all the repository names registered in the Nitrite store
     */
    Set<String> getRepositoryRegistry();

    /**
     * Returns a set of all the keyed-repository names registered in the Nitrite store.
     *
     * @return a set of all the keyed-repository names registered in the Nitrite store
     */
    Map<String, Set<String>> getKeyedRepositoryRegistry();

    /**
     * Checks if the store has any unsaved changes.
     *
     * @return {@code true} if the store has unsaved changes; {@code false} otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks if the store is opened in read-only mode.
     *
     * @return {@code true} if the store is read-only; {@code false} otherwise.
     */
    boolean isReadOnly();

    /**
     * Commits the changes. For persistent stores, it also writes
     * changes to disk. It does nothing if there are no unsaved changes.
     */
    void commit();

    /**
     * This method is called before closing the store. Any cleanup or finalization
     * tasks should be performed in this method.
     */
    void beforeClose();

    /**
     * Checks if a {@link NitriteMap} with the given name exists in the store.
     *
     * @param mapName the name of the map to check
     * @return true if the map exists, false otherwise
     */
    boolean hasMap(String mapName);

    /**
     * Opens a {@link NitriteMap}. The map is automatically created if 
     * it does not yet exist. If a map with this name is already opened, 
     * this map is returned.
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
     * Closes a {@link NitriteMap} with the specified name in the store.
     *
     * @param mapName the map name
     */
    void closeMap(String mapName);

    /**
     * Removes a {@link NitriteMap} with the specified name from the store.
     *
     * @param mapName the map name to remove.
     */
    void removeMap(String mapName);

    /**
     * Opens a {@link NitriteRTree} with the given key and value types. The key type must
     * extend the {@link BoundingBox} class. Returns a {@link NitriteRTree} instance that
     * can be used to perform R-Tree operations on the data. 
     * <p>
     * RTree is automatically created if it does not yet exist. If a 
     * RTree with this name is already open, this RTree is returned.
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
     * Closes a {@link NitriteRTree} with the specified name in the store.
     *
     * @param rTreeName the RTree name
     */
    void closeRTree(String rTreeName);

    /**
     * Removes a {@link NitriteRTree} with the specified name from the store.
     *
     * @param rTreeName the RTree name to remove.
     */
    void removeRTree(String rTreeName);

    /**
     * Subscribes a {@link StoreEventListener} to this store. The listener will be notified of any changes made to the store.
     *
     * @param listener the listener to subscribe
     */
    void subscribe(StoreEventListener listener);

    /**
     * Unsubscribes a {@link StoreEventListener} from this store.
     *
     * @param listener the listener to unsubscribe
     */
    void unsubscribe(StoreEventListener listener);

    /**
     * Gets the underlying storage engine version.
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
