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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;

/**
 * Represents an R-Tree in the nitrite database.
 *
 * @param <Key>   the type parameter
 * @param <Value> the type parameter
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public interface NitriteRTree<Key, Value> extends AutoCloseable {
    /**
     * Adds a key to the rtree.
     *
     * @param key       the key
     * @param nitriteId the nitrite id
     */
    void add(Key key, NitriteId nitriteId);

    /**
     * Removes a key from the rtree.
     *
     * @param key       the key
     * @param nitriteId the nitrite id
     */
    void remove(Key key, NitriteId nitriteId);

    /**
     * Finds the intersecting keys from the rtree.
     *
     * @param key the key
     * @return the record stream
     */
    RecordStream<NitriteId> findIntersectingKeys(Key key);

    /**
     * Finds the contained keys from the rtree.
     *
     * @param key the key
     * @return the record stream
     */
    RecordStream<NitriteId> findContainedKeys(Key key);

    /**
     * Gets the size of the rtree.
     *
     * @return the size
     */
    long size();

    /**
     * Closes this {@link NitriteRTree} instance.
     * */
    void close();

    /**
     * Clears the data.
     */
    void clear();

    /**
     * Drops this instance.
     */
    void drop();
}
