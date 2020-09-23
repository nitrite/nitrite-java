package org.dizitart.no2.store.memory;

import org.dizitart.no2.common.NullEntry;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.Comparables;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public class InMemoryMap<Key, Value> implements NitriteMap<Key, Value> {
    private final NavigableMap<Key, Value> backingMap;
    private final NavigableMap<NullEntry, Value> nullEntryMap;
    private final NitriteStore<?> nitriteStore;
    private final String mapName;

    public InMemoryMap(String mapName, NitriteStore<?> nitriteStore) {
        this.mapName = mapName;
        this.nitriteStore = nitriteStore;
        this.nullEntryMap = new ConcurrentSkipListMap<>();

        this.backingMap = new ConcurrentSkipListMap<>((o1, o2) ->
            Comparables.compare((Comparable<?>) o1, (Comparable<?>) o2));
    }

    @Override
    public boolean containsKey(Key key) {
        if (key == null) {
            return nullEntryMap.containsKey(NullEntry.getInstance());
        }
        return backingMap.containsKey(key);
    }

    @Override
    public Value get(Key key) {
        if (key == null) {
            return nullEntryMap.get(NullEntry.getInstance());
        }
        return backingMap.get(key);
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        backingMap.clear();
        nullEntryMap.clear();
        updateLastModifiedTime();
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<Value> values() {
        return RecordStream.fromCombined(backingMap.values(), nullEntryMap.values());
    }

    @Override
    public Value remove(Key key) {
        Value value;
        if (key == null) {
            value = nullEntryMap.remove(NullEntry.getInstance());
        } else {
            value = backingMap.remove(key);
        }
        updateLastModifiedTime();
        return value;
    }

    @Override
    public RecordStream<Key> keySet() {
        return RecordStream.fromIterable(() -> new Iterator<Key>() {
            final Iterator<Key> keyIterator = backingMap.keySet().iterator();
            final Iterator<NullEntry> nullEntryIterator = nullEntryMap.keySet().iterator();

            @Override
            public boolean hasNext() {
                boolean result = nullEntryIterator.hasNext();
                if (!result) {
                    return keyIterator.hasNext();
                }
                return true;
            }

            @Override
            public Key next() {
                if (nullEntryIterator.hasNext()) {
                    return null;
                } else {
                    return keyIterator.next();
                }
            }
        });
    }

    @Override
    public void put(Key key, Value value) {
        notNull(value, "value cannot be null");
        if (key == null) {
            nullEntryMap.put(NullEntry.getInstance(), value);
        } else {
            Map.Entry<Key, Value> firstEntry = backingMap.firstEntry();
            if (firstEntry != null) {
                if (!firstEntry.getKey().getClass().equals(key.getClass())) {
                    return;
                }
            }
            backingMap.put(key, value);
        }
        updateLastModifiedTime();
    }

    @Override
    public long size() {
        return backingMap.size() + nullEntryMap.size();
    }

    @Override
    public Value putIfAbsent(Key key, Value value) {
        notNull(value, "value cannot be null");

        Value v = get(key);
        if (v == null) {
            put(key, value);
        }
        updateLastModifiedTime();
        return v;
    }

    @Override
    public RecordStream<Pair<Key, Value>> entries() {
        return RecordStream.fromIterable(() -> new Iterator<Pair<Key, Value>>() {
            private final Iterator<Map.Entry<Key, Value>> entryIterator = backingMap.entrySet().iterator();
            private final Iterator<Map.Entry<NullEntry, Value>> nullEntryIterator = nullEntryMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                boolean result = nullEntryIterator.hasNext();
                if (!result) {
                    return entryIterator.hasNext();
                }
                return true;
            }

            @Override
            public Pair<Key, Value> next() {
                if (nullEntryIterator.hasNext()) {
                    Map.Entry<NullEntry, Value> entry = nullEntryIterator.next();
                    return new Pair<>(null, entry.getValue());
                } else {
                    Map.Entry<Key, Value> entry = entryIterator.next();
                    return new Pair<>(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @Override
    public Key higherKey(Key key) {
        if (key == null) {
            return null;
        }
        return backingMap.higherKey(key);
    }

    @Override
    public Key ceilingKey(Key key) {
        if (key == null) {
            return null;
        }
        return backingMap.ceilingKey(key);
    }

    @Override
    public Key lowerKey(Key key) {
        if (key == null) {
            return null;
        }
        return backingMap.lowerKey(key);
    }

    @Override
    public Key floorKey(Key key) {
        if (key == null) {
            return null;
        }
        return backingMap.floorKey(key);
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty() && nullEntryMap.isEmpty();
    }

    @Override
    public void drop() {
        clear();
        getStore().removeMap(mapName);
    }

    @Override
    public void close() {

    }
}
