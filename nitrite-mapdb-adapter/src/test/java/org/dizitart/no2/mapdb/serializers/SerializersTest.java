package org.dizitart.no2.mapdb.serializers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SerializersTest {
    @Test
    public void testFindSerializer() {
        assertTrue(Serializers.findSerializer(Object.class) instanceof org.mapdb.serializer.SerializerCompressionWrapper);
    }
}

