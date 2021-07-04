package org.dizitart.no2.store.memory;

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.Comparables;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * The in-memory {@link NitriteMap}.
 *
 * @param <Key>   the type parameter
 * @param <Value> the type parameter
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class InMemoryMap<Key, Value> implements NitriteMap<Key, Value> {
    private final NavigableMap<Key, Value> backingMap;
    private final NitriteStore<?> nitriteStore;
    private final String mapName;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;

    /**
     * Instantiates a new {@link InMemoryMap}.
     *
     * @param mapName      the map name
     * @param nitriteStore the nitrite store
     */
    public InMemoryMap(String mapName, NitriteStore<?> nitriteStore) {
        this.mapName = mapName;
        this.nitriteStore = nitriteStore;
        this.backingMap = new ConcurrentSkipListMap<>((o1, o2) ->
            Comparables.compare((Comparable<?>) o1, (Comparable<?>) o2));

        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
    }

    @Override
    public boolean containsKey(Key key) {
        return backingMap.containsKey(key);
    }

    @Override
    public Value get(Key key) {
        return backingMap.get(key);
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        backingMap.clear();
        updateLastModifiedTime();
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<Value> values() {
        return RecordStream.fromIterable(backingMap.values());
    }

    @Override
    public Value remove(Key key) {
        Value value = backingMap.remove(key);
        updateLastModifiedTime();
        return value;
    }

    @Override
    public RecordStream<Key> keys() {
        return RecordStream.fromIterable(backingMap.keySet());
    }

    @Override
    public void put(Key key, Value value) {
        notNull(value, "value cannot be null");
        backingMap.put(key, value);
        updateLastModifiedTime();
    }

    @Override
    public long size() {
        return backingMap.size();
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
        return getStream(backingMap);
    }

    @Override
    public RecordStream<Pair<Key, Value>> reversedEntries() {
        return getStream(backingMap.descendingMap());
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
        return backingMap.isEmpty();
    }

    @Override
    public void drop() {
        if (!droppedFlag.get()) {
            droppedFlag.compareAndSet(false, true);
            clear();
            getStore().removeMap(mapName);
        }
    }

    @Override
    public void close() {
        if (!closedFlag.get() && !droppedFlag.get()) {
            closedFlag.compareAndSet(false, true);
        }
    }

    private RecordStream<Pair<Key, Value>> getStream(NavigableMap<Key, Value> primaryMap) {
        return RecordStream.fromIterable(() -> new Iterator<Pair<Key, Value>>() {
            private final Iterator<Map.Entry<Key, Value>> entryIterator =
                primaryMap.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public Pair<Key, Value> next() {
                Map.Entry<Key, Value> entry = entryIterator.next();
                return new Pair<>(entry.getKey(), entry.getValue());
            }
        });
    }
}
