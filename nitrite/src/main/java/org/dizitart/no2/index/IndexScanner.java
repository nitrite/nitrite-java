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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Represents an index scanner during find operation.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class IndexScanner {
    private NitriteMap<Comparable<?>, ?> nitriteMap;
    private NavigableMap<Comparable<?>, ?> navigableMap;

    @Getter
    @Setter
    private boolean reverseScan;

    /**
     * Instantiates a new Index scanner.
     *
     * @param nitriteMap the nitrite map
     */
    public IndexScanner(NitriteMap<Comparable<?>, ?> nitriteMap) {
        this.nitriteMap = nitriteMap;
    }

    /**
     * Instantiates a new Index scanner.
     *
     * @param navigableMap the navigable map
     */
    public IndexScanner(NavigableMap<Comparable<?>, ?> navigableMap) {
        this.navigableMap = navigableMap;
    }

    /**
     * Get the largest key that is smaller than the given key, or null if no
     * such key exists.
     *
     * @param <T> the type parameter
     * @param key the key
     * @return the t
     */
    public <T extends Comparable<T>> T lowerKey(T key) {
        if (!reverseScan) {
            if (nitriteMap != null) {
                return (T) nitriteMap.lowerKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.lowerKey(key);
            }
        } else {
            if (nitriteMap != null) {
                return (T) nitriteMap.higherKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.higherKey(key);
            }
        }
        return null;
    }

    /**
     * Get the smallest key that is larger than the given key, or null if no
     * such key exists.
     *
     * @param <T> the type parameter
     * @param key the key
     * @return the t
     */
    public <T extends Comparable<T>> T higherKey(T key) {
        if (!reverseScan) {
            if (nitriteMap != null) {
                return (T) nitriteMap.higherKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.higherKey(key);
            }
        } else {
            if (nitriteMap != null) {
                return (T) nitriteMap.lowerKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.lowerKey(key);
            }
        }
        return null;
    }

    /**
     * Get the smallest key that is larger or equal to this key.
     *
     * @param <T> the type parameter
     * @param key the key
     * @return the t
     */
    public <T extends Comparable<T>> T ceilingKey(T key) {
        if (!reverseScan) {
            if (nitriteMap != null) {
                return (T) nitriteMap.ceilingKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.ceilingKey(key);
            }
        } else {
            if (nitriteMap != null) {
                return (T) nitriteMap.floorKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.floorKey(key);
            }
        }
        return null;
    }

    /**
     * Get the largest key that is smaller or equal to this key.
     *
     * @param <T> the type parameter
     * @param key the key
     * @return the t
     */
    public <T extends Comparable<T>> T floorKey(T key) {
        if (!reverseScan) {
            if (nitriteMap != null) {
                return (T) nitriteMap.floorKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.floorKey(key);
            }
        } else {
            if (nitriteMap != null) {
                return (T) nitriteMap.ceilingKey(key);
            } else if (navigableMap != null) {
                return (T) navigableMap.ceilingKey(key);
            }
        }
        return null;
    }

    /**
     * Gets the value mapped with the specified key or <code>null</code> otherwise.
     *
     * @param comparable the comparable
     * @return the object
     */
    public Object get(Comparable<?> comparable) {
        if (nitriteMap != null) {
            return nitriteMap.get(comparable);
        } else if (navigableMap != null) {
            return navigableMap.get(comparable);
        }
        return null;
    }

    /**
     * Returns the iterable entries of the indexed items.
     *
     * @return the iterable
     */
    public Iterable<? extends Pair<Comparable<?>, ?>> entries() {
        if (nitriteMap != null) {
            if (!reverseScan) {
                return nitriteMap.entries();
            } else {
                return nitriteMap.reversedEntries();
            }
        } else if (navigableMap != null) {
            Iterator<? extends Map.Entry<Comparable<?>, ?>> entryIterator;
            if (reverseScan) {
                entryIterator = navigableMap.descendingMap().entrySet().iterator();
            } else {
                entryIterator = navigableMap.entrySet().iterator();
            }

            return (Iterable<Pair<Comparable<?>, ?>>) () -> new Iterator<Pair<Comparable<?>, ?>>() {
                @Override
                public boolean hasNext() {
                    return entryIterator.hasNext();
                }

                @Override
                public Pair<Comparable<?>, ?> next() {
                    Map.Entry<Comparable<?>, ?> entry = entryIterator.next();
                    return Pair.pair(entry.getKey(), entry.getValue());
                }
            };
        }
        return null;
    }
}
