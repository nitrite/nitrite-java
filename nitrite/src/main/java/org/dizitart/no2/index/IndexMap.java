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

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class IndexMap {
    private NitriteMap<DBValue, ?> nitriteMap;
    private NavigableMap<DBValue, ?> navigableMap;

    // composite layout (non-unique single-field index): the backing map is keyed by
    // (value, id) pairs (see IndexEntryKey). This IndexMap still presents the classic
    // value -> List<NitriteId> view to the scanner and the filters.
    private NitriteMap<IndexEntryKey, ?> compositeMap;

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

    private IndexMap(NitriteMap<IndexEntryKey, ?> compositeMap, boolean composite) {
        this.compositeMap = compositeMap;
    }

    /**
     * Instantiates an {@link IndexMap} over a non-unique index stored in the composite-key
     * layout (one row per {@code (value, id)} pair). The returned map translates between the
     * caller's value-keyed view and the underlying {@link IndexEntryKey} rows.
     *
     * @param compositeMap the backing composite map
     * @return the index map
     */
    public static IndexMap composite(NitriteMap<IndexEntryKey, ?> compositeMap) {
        return new IndexMap(compositeMap, true);
    }

    public DBValue firstKey() {
        if (compositeMap != null) {
            IndexEntryKey first = compositeMap.firstKey();
            return first == null ? DBNull.getInstance() : first.getValue();
        }
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
        if (compositeMap != null) {
            IndexEntryKey last = compositeMap.lastKey();
            return last == null ? DBNull.getInstance() : last.getValue();
        }
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
        if (compositeMap != null) {
            // largest distinct value strictly less than `key`: the lower bracket sorts before
            // every (key, id) row, so the largest underlying key below it belongs to a smaller value
            IndexEntryKey k = compositeMap.lowerKey(IndexEntryKey.lowerBound(dbKey));
            return k == null ? DBNull.getInstance() : k.getValue();
        }
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
        if (compositeMap != null) {
            // first distinct value strictly greater than `key`: every (key, id) row sorts at or
            // below the upper bracket, so the first underlying key past it belongs to the next value
            IndexEntryKey k = compositeMap.higherKey(IndexEntryKey.upperBound(dbKey));
            return k == null ? DBNull.getInstance() : k.getValue();
        }
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
        if (compositeMap != null) {
            // first distinct value >= `key`: the lower bracket sorts before every (key, id) row
            IndexEntryKey k = compositeMap.ceilingKey(IndexEntryKey.lowerBound(dbKey));
            return k == null ? DBNull.getInstance() : k.getValue();
        }
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
        if (compositeMap != null) {
            // largest distinct value <= `key`: every (key, id) row sorts at or below the upper bracket
            IndexEntryKey k = compositeMap.floorKey(IndexEntryKey.upperBound(dbKey));
            return k == null ? DBNull.getInstance() : k.getValue();
        }
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
        if (compositeMap != null) {
            return compositeGet(dbValue == null ? DBNull.getInstance() : dbValue);
        }
        if (nitriteMap != null) {
            return nitriteMap.get(dbValue);
        } else if (navigableMap != null) {
            return navigableMap.get(dbValue);
        }
        return null;
    }

    /**
     * Equality lookup in the composite layout: range-scan the leading-value group and collect
     * all the trailing ids, returning the same {@code List<NitriteId>} the classic layout would.
     */
    private List<NitriteId> compositeGet(DBValue value) {
        List<NitriteId> nitriteIds = null;
        IndexEntryKey key = compositeMap.ceilingKey(IndexEntryKey.lowerBound(value));
        while (key != null && key.getValue().compareTo(value) == 0) {
            // navigation over a transactional map can surface keys removed in the current
            // transaction; the stored value is null for those, so skip them to keep the id set
            // (and any covered-count short-circuit built from it) exact.
            if (compositeMap.get(key) != null) {
                if (nitriteIds == null) {
                    nitriteIds = new ArrayList<>();
                }
                nitriteIds.add(key.getNitriteId());
            }
            key = compositeMap.higherKey(key);
        }
        return nitriteIds;
    }

    /**
     * Returns the iterable entries of the indexed items.
     *
     * @return the iterable
     */
    public Iterable<? extends Pair<DBValue, ?>> entries() {
        if (compositeMap != null) {
            return compositeEntries();
        }
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
     * Groups the sorted flat {@code (value, id)} rows back into one {@code (value, List<ids>)}
     * entry per distinct value, so callers (full scans, not-equals/not-in) see the same shape
     * as the classic layout.
     */
    private Iterable<Pair<DBValue, ?>> compositeEntries() {
        Iterator<? extends Pair<IndexEntryKey, ?>> rowIterator = reverseScan
            ? compositeMap.reversedEntries().iterator()
            : compositeMap.entries().iterator();

        return () -> new Iterator<>() {
            // one-row lookahead buffer used to detect the end of a leading-value group
            private Pair<IndexEntryKey, ?> pending = rowIterator.hasNext() ? rowIterator.next() : null;

            @Override
            public boolean hasNext() {
                return pending != null;
            }

            @Override
            public Pair<DBValue, ?> next() {
                if (pending == null) {
                    throw new NoSuchElementException();
                }
                DBValue groupValue = pending.getFirst().getValue();
                List<NitriteId> ids = new ArrayList<>();
                ids.add(pending.getFirst().getNitriteId());
                pending = rowIterator.hasNext() ? rowIterator.next() : null;
                while (pending != null && pending.getFirst().getValue().compareTo(groupValue) == 0) {
                    ids.add(pending.getFirst().getNitriteId());
                    pending = rowIterator.hasNext() ? rowIterator.next() : null;
                }
                DBValue key = groupValue instanceof DBNull ? null : groupValue;
                return new Pair<>(key, ids);
            }
        };
    }

    /**
     * Gets the terminal nitrite ids from this map.
     *
     * @return the terminal nitrite ids
     */
    public List<NitriteId> getTerminalNitriteIds() {
        List<NitriteId> terminalResult = new ArrayList<>();

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
