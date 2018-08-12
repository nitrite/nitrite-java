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

package org.dizitart.no2.store;

import org.dizitart.no2.meta.Attributes;

import java.util.Set;

/**
 * Represents a persistent storage for Nitrite database.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see NitriteMVStore
 */
public interface NitriteStore {
    /**
     * Gets the set of all map names.
     *
     * @return the set of names.
     */
    Set<String> getMapNames();

    /**
     * Checks whether there are any unsaved changes.
     *
     * @return `true` if there are any changes; `false` otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether this store is closed for further modification.
     *
     * @return `true` if closed; `false` otherwise.
     */
    boolean isClosed();

    /**
     * Compacts the store by moving all chunks next to each other.
     */
    void compact();

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
     * Closes the file and the store, without writing anything. This method
     * ignores all errors.
     */
    void closeImmediately();

    /**
     * Checks whether a given map exists in the store.
     *
     * @param mapName the map name
     * @return `true` if it exists; `false` otherwise.
     */
    boolean hasMap(String mapName);

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
     * @param <Key>      the key type
     * @param <Value>    the value type
     * @param nitriteMap the map to remove.
     */
    <Key, Value> void removeMap(NitriteMap<Key, Value> nitriteMap);

    /**
     * Gets the metadata of all {@link NitriteMap}s.
     *
     * @return meta data of all maps.
     * */
    NitriteMap<String, Attributes> metaMap();
}
