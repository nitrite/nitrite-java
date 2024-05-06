package org.dizitart.no2.rocksdb.serializers.fury;

import org.apache.fury.Fury;
import org.apache.fury.ThreadLocalFury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.rocksdb.serializers.ObjectSerializer;

import java.util.Arrays;

import static org.dizitart.no2.rocksdb.Constants.DB_NULL;

public class FuryObjectSerializer implements ObjectSerializer {

    private final ThreadSafeFury fury = new ThreadLocalFury(classLoader -> {
        Fury f = Fury.builder().withLanguage(Language.JAVA)
                .withClassLoader(classLoader)
                .requireClassRegistration(false)
                .build();
        RegisterNitriteSerializers.now(f);
        return f;
    });

    public FuryObjectSerializer() {

    }

    @Override
    public <T> byte[] encode(T object) {
        if (object == null) return DB_NULL;

        try {
            return fury.serialize(object);
        } catch (Exception e) {
            throw new NitriteIOException("Failed to serialize object", e);
        }
    }

    @Override
    public <T> byte[] encodeKey(T object) {
        return encode(object);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] bytes, Class<T> type) {
        if (Arrays.equals(bytes, DB_NULL)) return null;

        try {
            return (T) fury.deserialize(bytes);
        } catch (Exception e) {
            throw new NitriteIOException("Failed to deserialize object", e);
        }
    }

    @Override
    public <T> T decodeKey(byte[] bytes, Class<T> type) {
        return decode(bytes, type);
    }

}
