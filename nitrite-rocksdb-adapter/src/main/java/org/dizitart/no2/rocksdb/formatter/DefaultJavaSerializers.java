package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.util.UUID;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class DefaultJavaSerializers {

    private static class UUIDSerializer extends Serializer<UUID> {

        @Override
        public void write(Kryo kryo, Output output, UUID object) {
            output.writeString(object.toString());
        }

        @Override
        public UUID read(Kryo kryo, Input input, Class<? extends UUID> type) {
            return UUID.fromString(input.readString());
        }
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(UUID.class, new UUIDSerializer());
    }
}
