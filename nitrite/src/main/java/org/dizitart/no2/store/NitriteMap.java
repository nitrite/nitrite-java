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

import org.dizitart.no2.meta.MetadataAware;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Nitrite key-value pair map. Every piece of
 * data in a Nitrite database is stored in {@link NitriteMap}.
 *
 * @param <Key>   the type of key
 * @param <Value> the type of value
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface NitriteMap<Key, Value> extends MetadataAware {
    /**
     * Determines if the map contains a mapping for the
     * specified key.
     *
     * @param key key whose presence in this map is to be tested
     * @return `true` if this map contains a mapping for the specified key.
     */
    boolean containsKey(Key key);

    /**
     * Gets the value mapped with the specified key or `null` otherwise.
     *
     * @param key the key
     * @return the value, or `null` if the key not found.
     */
    Value get(Key key);

    /**
     * Gets the parent {@link NitriteStore} where this map is stored.
     *
     * @return the store where this map is stored.
     */
    NitriteStore getStore();

    /**
     * Remove all entries in the map.
     */
    void clear();

    /**
     * Gets name of this map.
     *
     * @return the name of this map.
     */
    String getName();

    /**
     * Gets a {@link Collection} view of the values contained in
     * this map.
     *
     * @return the collection view of all values in this map.
     */
    Collection<Value> values();

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key the key whose mapping is to be removed from this map.
     * @return the value that has been removed.
     */
    Value remove(Key key);

    /**
     * Gets a {@link Set} view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    Set<Key> keySet();

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key   key with which the specified value is to be associated (may not be null).
     * @param value value to be associated with the specified key (may not be null).
     */
    void put(Key key, Value value);

    /**
     * Get the number of entries, as a integer. Integer.MAX_VALUE is returned if
     * there are more than this entries.
     *
     * @return the number of entries, as an integer.
     */
    int size();

    /**
     * Get the number of entries, as a long.
     *
     * @return the number of entries
     */
    long sizeAsLong();

    /**
     * Add a key-value pair if it does not yet exist.
     *
     * @param key   the key (may not be null)
     * @param value the new value
     * @return the old value if the key existed, or `null` otherwise.
     */
    Value putIfAbsent(Key key, Value value);

    /**
     * Gets a {@link Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    Set<Map.Entry<Key, Value>> entrySet();

    /**
     * Get the smallest key that is larger than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the result.
     */
    Key higherKey(Key key);

    /**
     * Get the smallest key that is larger or equal to this key.
     *
     * @param key the key
     * @return the result.
     */
    Key ceilingKey(Key key);

    /**
     * Get the largest key that is smaller than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the result.
     */
    Key lowerKey(Key key);

    /**
     * Get the largest key that is smaller or equal to this key.
     *
     * @param key the key
     * @return the result.
     */
    Key floorKey(Key key);

    /**
     * Indicates whether the map is empty.
     *
     * @return `true` if the map is empty; `false` otherwise.
     * */
    boolean isEmpty();

    /**
     * Returns the list of all keys in the map.
     *
     * @return list of all keys.
     * */
    List<Key> keyList();

    /**
     * Deletes the map from the store.
     * */
    void drop();
}
