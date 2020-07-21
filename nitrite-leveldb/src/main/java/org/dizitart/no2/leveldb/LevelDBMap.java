package org.dizitart.no2.leveldb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;
import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.util.*;

@Slf4j
public class LevelDBMap<K, V> implements NitriteMap<K, V> {
    private static final byte[] DB_NULL = new byte[0];

    private final SubLevelDB db;
    private final FSTConfiguration configuration;
    private final DBComparator comparator;
    private final NitriteStore store;

    public LevelDBMap(SubLevelDB subLevelDB, FSTConfiguration conf, NitriteStore store) {
        this.db = subLevelDB;
        this.configuration = conf;
        this.store = store;
        this.comparator = new DBComparator() {
            @Override
            public String name() {
                return "no2-leveldb-comparator";
            }

            @Override
            public byte[] findShortestSeparator(byte[] start, byte[] limit) {
                return start;
            }

            @Override
            public byte[] findShortSuccessor(byte[] key) {
                return key;
            }

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public int compare(byte[] key1, byte[] key2) {
                Comparable k1 = (Comparable) conf.asObject(key1);
                Comparable k2 = (Comparable) conf.asObject(key2);

                return k1.compareTo(k2);
            }
        };
    }

    @Override
    public boolean containsKey(K k) {
        byte[] key = configuration.asByteArray(k);
        return db.get(key) == null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K k) {
        byte[] key = configuration.asByteArray(k);
        byte[] value = db.get(key);
        if (Arrays.equals(DB_NULL, value) || value == null) {
            return null;
        }

        return (V) configuration.asObject(value);
    }

    @Override
    public NitriteStore getStore() {
        return store;
    }

    @Override
    public void clear() {
        try (WriteBatch batch = db.createWriteBatch()) {
            try (DBIterator iterator = db.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    batch.delete(iterator.peekNext().getKey());
                }
            }
            db.write(batch);
        } catch (IOException e) {
            log.error("Failed to clear nitrite map", e);
        }
    }

    @Override
    public String getName() {
        return db.getPrefix();
    }

    @Override
    public RecordStream<V> values() {
        return RecordStream.fromIterable(new ValueSet<V>(db, this, configuration));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(K k) {
        byte[] key = configuration.asByteArray(k);
        byte[] value = db.get(key);

        db.delete(key);
        if (Arrays.equals(DB_NULL, value)) {
            return null;
        }

        return (V) configuration.asObject(value);
    }

    @Override
    public RecordStream<K> keySet() {
        return RecordStream.fromIterable(new KeySet<K>(db, this, configuration));
    }

    @Override
    public void put(K k, V v) {
        if (k == null) {
            throw new InvalidOperationException("key cannot be null");
        }

        byte[] key = configuration.asByteArray(k);
        byte[] value = v == null ? DB_NULL : configuration.asByteArray(v);
        db.put(key, value);
    }

    @Override
    public long size() {
        int c = 0;
        try (DBIterator iterator = db.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                c++;
            }
        } catch (IOException e) {
            log.error("Error while calculating size", e);
            throw new NitriteIOException("failed to calculate size", e);
        }
        return c;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K k, V v) {
        if (k == null) {
            throw new InvalidOperationException("key cannot be null");
        }

        byte[] key = configuration.asByteArray(k);
        byte[] oldValue = db.get(key);
        if (oldValue == null) {
            byte[] value = configuration.asByteArray(v);
            db.put(key, value);
            return null;
        }
        return (V) configuration.asObject(oldValue);
    }

    @Override
    public RecordStream<KeyValuePair<K, V>> entries() {
        return RecordStream.fromIterable(new EntrySet<K, V>(db, this, configuration));
    }

    @Override
    @SuppressWarnings("unchecked")
    public K higherKey(K k) {
        try (DBIterator iterator = db.iterator()) {
            byte[] key = configuration.asByteArray(k);
            iterator.seek(key);
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                byte[] nextKey = next.getKey();
                if (comparator.compare(nextKey, key) > 0) {
                    return (K) configuration.asObject(nextKey);
                }
            }
        } catch (IOException e) {
            log.error("Error while fetching higher key", e);
            throw new NitriteIOException("failed to get higher key", e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K ceilingKey(K k) {
        try (DBIterator iterator = db.iterator()) {
            byte[] key = configuration.asByteArray(k);
            iterator.seek(key);
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                byte[] nextKey = next.getKey();
                if (comparator.compare(nextKey, key) >= 0) {
                    return (K) configuration.asObject(nextKey);
                }
            }
        } catch (IOException e) {
            log.error("Error while fetching ceiling key", e);
            throw new NitriteIOException("failed to get ceiling key", e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K lowerKey(K k) {
        try (DBIterator iterator = db.iterator()) {
            Map.Entry<byte[], byte[]> previous = null;
            byte[] key = configuration.asByteArray(k);

            iterator.seekToFirst();
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                byte[] nextKey = next.getKey();

                if (comparator.compare(key, nextKey) < 0) {
                    previous = next;
                } else if (previous != null) {
                    byte[] previousKey = previous.getKey();
                    return (K) configuration.asObject(previousKey);
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("Error while fetching lower key", e);
            throw new NitriteIOException("failed to get lower key", e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K floorKey(K k) {
        try (DBIterator iterator = db.iterator()) {
            Map.Entry<byte[], byte[]> previous = null;
            byte[] key = configuration.asByteArray(k);

            iterator.seekToFirst();
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                byte[] nextKey = next.getKey();

                if (comparator.compare(key, nextKey) <= 0) {
                    previous = next;
                } else if (previous != null) {
                    byte[] previousKey = previous.getKey();
                    return (K) configuration.asObject(previousKey);
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("Error while fetching floor key", e);
            throw new NitriteIOException("failed to get floor key", e);
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        try (DBIterator iterator = db.iterator()) {
            iterator.seekToFirst();
            return !iterator.hasNext();
        } catch (IOException e) {
            log.error("Error while checking empty", e);
            throw new NitriteIOException("failed to check empty", e);
        }
    }

    @Override
    public void drop() {
        clear();
        store.removeMap(db.getPrefix());
    }

    @Override
    public Attributes getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(Attributes attributes) {

    }

}
