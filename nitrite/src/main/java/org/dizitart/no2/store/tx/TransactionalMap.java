package org.dizitart.no2.store.tx;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Anindya Chatterjee
 */
@SuppressWarnings("SortedCollectionWithNonComparableKeys")
public class TransactionalMap<K, V> implements NitriteMap<K, V> {
    private final NitriteMap<K, V> primary;
    private final NavigableMap<K, V> backingMap;
    private final String mapName;
    private final NitriteStore<?> store;

    public TransactionalMap(String mapName, NitriteMap<K, V> primary, NitriteStore<?> store) {
        this.primary = primary;
        this.backingMap = new ConcurrentSkipListMap<>();
        this.mapName = mapName;
        this.store = store;
    }

    @Override
    public boolean containsKey(K k) {
        boolean result = primary != null && primary.containsKey(k);
        if (!result) {
            return backingMap.containsKey(k);
        }
        return true;
    }

    @Override
    public V get(K k) {
        V result = primary != null ? primary.get(k) : null;
        if (result == null) {
            return backingMap.get(k);
        }
        return result;
    }

    @Override
    public NitriteStore<?> getStore() {
        return store;
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<V> values() {
        return RecordStream.fromCombined(primary != null ? primary.values() : Collections.emptyList(),
            backingMap.values());
    }

    @Override
    public V remove(K k) {
        return backingMap.remove(k);
    }

    @Override
    public RecordStream<K> keySet() {
        return RecordStream.fromCombined(primary != null ? primary.keySet() : Collections.emptySet(),
            backingMap.keySet());
    }

    @Override
    public void put(K k, V v) {
        backingMap.put(k, v);
    }

    @Override
    public long size() {
        return backingMap.size();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null) {
            put(key, value);
        }

        return v;
    }

    @Override
    public RecordStream<Pair<K, V>> entries() {
        return () -> new Iterator<Pair<K, V>>() {
            private final Iterator<Pair<K, V>> primaryIterator =
                primary != null ? primary.entries().iterator() : Collections.emptyIterator();
            private final Iterator<Map.Entry<K, V>> iterator = backingMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                boolean result = primaryIterator.hasNext();
                if (!result) {
                    return iterator.hasNext();
                }
                return true;
            }

            @Override
            public Pair<K, V> next() {
                Pair<K, V> next = primaryIterator.hasNext() ? primaryIterator.next() : null;
                if (next == null) {
                    Map.Entry<K, V> entry = iterator.next();
                    return new Pair<>(entry.getKey(), entry.getValue());
                }
                return next;
            }
        };
    }

    @Override
    public K higherKey(K k) {
        K primaryKey = primary != null ? primary.higherKey(k) : null;
        K backingKey = backingMap.higherKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.higher(k);
    }

    @Override
    public K ceilingKey(K k) {
        K primaryKey = primary != null ? primary.ceilingKey(k) : null;
        K backingKey = backingMap.ceilingKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.ceiling(k);
    }

    @Override
    public K lowerKey(K k) {
        K primaryKey = primary != null ? primary.lowerKey(k) : null;
        K backingKey = backingMap.lowerKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.lower(k);
    }

    @Override
    public K floorKey(K k) {
        K primaryKey = primary != null ? primary.floorKey(k) : null;
        K backingKey = backingMap.floorKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.floor(k);
    }

    @Override
    public boolean isEmpty() {
        boolean result = primary == null || primary.isEmpty();
        if (result) {
            return backingMap.isEmpty();
        }
        return false;
    }

    @Override
    public void drop() {
        backingMap.clear();
    }

    @Override
    public void close() {
        backingMap.clear();
    }
}
