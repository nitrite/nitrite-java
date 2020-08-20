package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Anindya Chatterjee
 */
public abstract class KryoKeySerializer<T> extends Serializer<T> {
    public abstract void writeKey(Kryo kryo, Output output, T object);
    public abstract T readKey(Kryo kryo, Input input, Class<T> type);

    public boolean registerToKryo() {
        return false;
    }
}
