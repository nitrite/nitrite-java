package org.dizitart.no2.mapdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MapDBMap<K, V> implements NitriteMap<K, V> {


    @Override
    public boolean containsKey(K k) {
        return false;
    }

    @Override
    public V get(K k) {
        return null;
    }

    @Override
    public NitriteStore<?> getStore() {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public RecordStream<V> values() {
        return null;
    }

    @Override
    public V remove(K k) {
        return null;
    }

    @Override
    public RecordStream<K> keySet() {
        return null;
    }

    @Override
    public void put(K k, V v) {

    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public V putIfAbsent(K k, V v) {
        return null;
    }

    @Override
    public RecordStream<KeyValuePair<K, V>> entries() {
        return null;
    }

    @Override
    public K higherKey(K k) {
        return null;
    }

    @Override
    public K ceilingKey(K k) {
        return null;
    }

    @Override
    public K lowerKey(K k) {
        return null;
    }

    @Override
    public K floorKey(K k) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void drop() {

    }

    @Override
    public void close() {

    }
}
