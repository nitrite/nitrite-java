package org.dizitart.no2.mapdb.serializers;

import org.mapdb.serializer.GroupSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
public class Serializers {
    private static final Map<String, GroupSerializer<?>> serializerRegistry;
    private Serializers() {
    }

    static {
        serializerRegistry = new HashMap<>();
    }

    public static void registerSerializer(Class<?> type, GroupSerializer<?> serializer) {
        serializerRegistry.put(type.getName(), serializer);
    }

    public static GroupSerializer<?> findSerializer(Class<?> type) {
        if (serializerRegistry.containsKey(type.getName())) {
            return serializerRegistry.get(type.getName());
        }
        return null;
    }
}
