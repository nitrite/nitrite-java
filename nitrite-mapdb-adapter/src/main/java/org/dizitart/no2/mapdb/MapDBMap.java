package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.mapdb.BTreeMap;
import org.mapdb.DBException;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MapDBMap<K, V> implements NitriteMap<K, V> {
    @Getter(AccessLevel.PACKAGE)
    private final BTreeMap<K, V> bTreeMap;

    @Getter(AccessLevel.PACKAGE)
    private final BTreeMap<DBNull, V> nullEntryMap;

    private final NitriteStore<?> nitriteStore;
    private final String mapName;


    public MapDBMap(String mapName, BTreeMap<K, V> bTreeMap,
                    BTreeMap<DBNull, V> nullEntryMap, NitriteStore<?> nitriteStore) {
        this.bTreeMap = bTreeMap;
        this.mapName = mapName;
        this.nitriteStore = nitriteStore;
        this.nullEntryMap = nullEntryMap;
    }

    @Override
    public boolean containsKey(K k) {
        if (k == null) {
            return nullEntryMap.containsKey(DBNull.getInstance());
        }
        return bTreeMap.containsKey(k);
    }

    @Override
    public V get(K k) {
        if (k == null) {
            return nullEntryMap.get(DBNull.getInstance());
        }

        Map.Entry<K, V> firstEntry = bTreeMap.firstEntry();
        if (firstEntry != null) {
            K firstKey = firstEntry.getKey();
            if (firstKey.getClass().equals(k.getClass())) {
                return bTreeMap.get(k);
            }
        }
        return null;
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        bTreeMap.clear();
        nullEntryMap.clear();
        updateLastModifiedTime();
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<V> values() {
        return RecordStream.fromCombined(bTreeMap.values(), nullEntryMap.values());
    }

    @Override
    public V remove(K k) {
        V value;
        if (k == null) {
            value = nullEntryMap.remove(DBNull.getInstance());
        } else {
            value = bTreeMap.remove(k);
        }
        updateLastModifiedTime();
        return value;
    }

    @Override
    public RecordStream<K> keys() {
        return RecordStream.fromIterable(() -> new Iterator<K>() {
            final Iterator<K> keyIterator = bTreeMap.keyIterator();
            final Iterator<DBNull> nullEntryIterator = nullEntryMap.keyIterator();

            @Override
            public boolean hasNext() {
                boolean result = nullEntryIterator.hasNext();
                if (!result) {
                    return keyIterator.hasNext();
                }
                return true;
            }

            @Override
            public K next() {
                if (nullEntryIterator.hasNext()) {
                    return null;
                } else {
                    return keyIterator.next();
                }
            }
        });
    }

    @Override
    public void put(K k, V v) {
        notNull(v, "value cannot be null");
        try {
            if (k == null) {
                nullEntryMap.put(DBNull.getInstance(), v);
            } else {
                bTreeMap.put(k, v);
            }
            updateLastModifiedTime();
        } catch (DBException e) {
            log.error("Error while writing data", e);
            throw new NitriteIOException("failed to write data", e);
        }
    }

    @Override
    public long size() {
        return bTreeMap.sizeLong() + nullEntryMap.sizeLong();
    }

    @Override
    public V putIfAbsent(K k, V v) {
        notNull(v, "value cannot be null");

        V value;
        if (k == null) {
            value = nullEntryMap.putIfAbsent(DBNull.getInstance(), v);
        } else {
            value = bTreeMap.putIfAbsent(k, v);
        }
        updateLastModifiedTime();
        return value;
    }

    @Override
    public RecordStream<Pair<K, V>> entries() {
        return getStream(bTreeMap, nullEntryMap);
    }

    @Override
    public RecordStream<Pair<K, V>> reversedEntries() {
        return getStream(bTreeMap.descendingMap(), nullEntryMap.descendingMap());
    }

    @Override
    public K higherKey(K k) {
        if (k == null) {
            return null;
        }
        return bTreeMap.higherKey(k);
    }

    @Override
    public K ceilingKey(K k) {
        if (k == null) {
            return null;
        }
        return bTreeMap.ceilingKey(k);
    }

    @Override
    public K lowerKey(K k) {
        if (k == null) {
            return null;
        }
        return bTreeMap.lowerKey(k);
    }

    @Override
    public K floorKey(K k) {
        if (k == null) {
            return null;
        }
        return bTreeMap.floorKey(k);
    }

    @Override
    public boolean isEmpty() {
        return bTreeMap.isEmpty() && nullEntryMap.isEmpty();
    }

    @Override
    public void drop() {
        nitriteStore.removeMap(getName());
    }

    @Override
    public void close() {
        bTreeMap.close();
        nullEntryMap.close();
    }

    private RecordStream<Pair<K, V>> getStream(NavigableMap<K, V> primaryMap,
                                               NavigableMap<DBNull, V> nullEntryMap) {
        return RecordStream.fromIterable(() -> new Iterator<Pair<K, V>>() {
            private final Iterator<Map.Entry<K, V>> entryIterator =
                primaryMap.entrySet().iterator();
            private final Iterator<Map.Entry<DBNull, V>> nullEntryIterator =
                nullEntryMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                boolean result = nullEntryIterator.hasNext();
                if (!result) {
                    return entryIterator.hasNext();
                }
                return true;
            }

            @Override
            public Pair<K, V> next() {
                if (nullEntryIterator.hasNext()) {
                    Map.Entry<DBNull, V> entry = nullEntryIterator.next();
                    return new Pair<>(null, entry.getValue());
                } else {
                    Map.Entry<K, V> entry = entryIterator.next();
                    return new Pair<>(entry.getKey(), entry.getValue());
                }
            }
        });
    }
}
