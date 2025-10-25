/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.index;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class IndexMap {
    private NitriteMap<DBValue, ?> nitriteMap;
    private NavigableMap<DBValue, ?> navigableMap;

    @Getter
    @Setter
    private boolean reverseScan;

    /**
     * Instantiates a new {@link IndexMap}.
     *
     * @param nitriteMap the nitrite map
     */
    public IndexMap(NitriteMap<DBValue, ?> nitriteMap) {
        this.nitriteMap = nitriteMap;
    }

    /**
     * Instantiates a new {@link IndexMap}.
     *
     * @param navigableMap the navigable map
     */
    public IndexMap(NavigableMap<DBValue, ?> navigableMap) {
        this.navigableMap = navigableMap;
    }

    public DBValue firstKey() {
        DBValue dbKey;
        if (nitriteMap != null) {
            dbKey = nitriteMap.firstKey();
        } else if (navigableMap != null) {
            dbKey = navigableMap.firstKey();
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    public DBValue lastKey() {
        DBValue dbKey;
        if (nitriteMap != null) {
            dbKey = nitriteMap.lastKey();
        } else if (navigableMap != null) {
            dbKey = navigableMap.lastKey();
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    /**
     * Get the largest key that is smaller than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the t
     */
    public DBValue lowerKey(DBValue key) {
        DBValue dbKey = key == null ? DBNull.getInstance() : key;
        if (nitriteMap != null) {
            dbKey = nitriteMap.lowerKey(dbKey);
        } else if (navigableMap != null) {
            dbKey = navigableMap.lowerKey(dbKey);
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    /**
     * Get the smallest key that is larger than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the t
     */
    public DBValue higherKey(DBValue key) {
        DBValue dbKey = key == null ? DBNull.getInstance() : key;
        if (nitriteMap != null) {
            dbKey = nitriteMap.higherKey(dbKey);
        } else if (navigableMap != null) {
            dbKey = navigableMap.higherKey(dbKey);
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    /**
     * Get the smallest key that is larger or equal to this key.
     *
     * @param key the key
     * @return the t
     */
    public DBValue ceilingKey(DBValue key) {
        DBValue dbKey = key == null ? DBNull.getInstance() : key;
        if (nitriteMap != null) {
            dbKey = nitriteMap.ceilingKey(dbKey);
        } else if (navigableMap != null) {
            dbKey = navigableMap.ceilingKey(dbKey);
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    /**
     * Get the largest key that is smaller or equal to this key.
     *
     * @param key the key
     * @return the t
     */
    public DBValue floorKey(DBValue key) {
        DBValue dbKey = key == null ? DBNull.getInstance() : key;
        if (nitriteMap != null) {
            dbKey = nitriteMap.floorKey(dbKey);
        } else if (navigableMap != null) {
            dbKey = navigableMap.floorKey(dbKey);
        } else {
            dbKey = null;
        }
        return dbKey == null ? DBNull.getInstance() : dbKey;
    }

    /**
     * Gets the value mapped with the specified key or <code>null</code> otherwise.
     *
     * @param dbValue the db value
     * @return the object
     */
    public Object get(DBValue dbValue) {
        if (nitriteMap != null) {
            return nitriteMap.get(dbValue);
        } else if (navigableMap != null) {
            return navigableMap.get(dbValue);
        }
        return null;
    }

    /**
     * Returns the iterable entries of the indexed items.
     *
     * @return the iterable
     */
    public Iterable<? extends Pair<DBValue, ?>> entries() {
        if (nitriteMap != null) {
            Iterator<? extends Pair<DBValue, ?>> entryIterator;
            if (!reverseScan) {
                entryIterator = nitriteMap.entries().iterator();
            } else {
                entryIterator = nitriteMap.reversedEntries().iterator();
            }

            return (Iterable<Pair<DBValue, ?>>) () -> new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return entryIterator.hasNext();
                }

                @Override
                public Pair<DBValue, ?> next() {
                    Pair<DBValue, ?> next = entryIterator.next();
                    DBValue dbKey = next.getFirst();
                    if (dbKey instanceof DBNull) {
                        return new Pair<>(null, next.getSecond());
                    } else {
                        return new Pair<>(dbKey, next.getSecond());
                    }
                }
            };
        } else if (navigableMap != null) {
            Iterator<? extends Map.Entry<DBValue, ?>> entryIterator;
            if (reverseScan) {
                entryIterator = navigableMap.descendingMap().entrySet().iterator();
            } else {
                entryIterator = navigableMap.entrySet().iterator();
            }

            return (Iterable<Pair<DBValue, ?>>) () -> new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return entryIterator.hasNext();
                }

                @Override
                public Pair<DBValue, ?> next() {
                    Map.Entry<DBValue, ?> next = entryIterator.next();
                    DBValue dbKey = next.getKey();
                    if (dbKey instanceof DBNull) {
                        return new Pair<>(null, next.getValue());
                    } else {
                        return new Pair<>(dbKey, next.getValue());
                    }
                }
            };
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Gets the terminal nitrite ids from this map.
     *
     * @return the terminal nitrite ids
     */
    public List<NitriteId> getTerminalNitriteIds() {
        List<NitriteId> terminalResult = new CopyOnWriteArrayList<>();

        // scan each entry of the navigable map and collect all terminal nitrite-ids
        for (Pair<DBValue, ?> entry : entries()) {
            // if the value is terminal, collect all nitrite-ids
            if (entry.getSecond() instanceof List) {
                List<NitriteId> nitriteIds = (List<NitriteId>) entry.getSecond();
                terminalResult.addAll(nitriteIds);
            }

            // if the value is not terminal, scan recursively
            if (entry.getSecond() instanceof NavigableMap) {
                NavigableMap<DBValue, ?> subMap = (NavigableMap<DBValue, ?>) entry.getSecond();
                IndexMap indexMap = new IndexMap(subMap);
                List<NitriteId> nitriteIds = indexMap.getTerminalNitriteIds();
                terminalResult.addAll(nitriteIds);
            }
        }

        return terminalResult;
    }
}
