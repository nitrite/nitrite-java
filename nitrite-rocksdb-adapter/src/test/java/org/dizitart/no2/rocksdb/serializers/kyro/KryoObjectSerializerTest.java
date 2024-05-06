package org.dizitart.no2.rocksdb.serializers.kyro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KryoObjectSerializerTest {

    @Test
    public void testEncode() {
        assertEquals(6, (new KryoObjectSerializer()).<Object>encode("object").length);
        assertEquals(1, (new KryoObjectSerializer()).encode(null).length);
    }

    @Test
    public void testEncodeKey() {
        assertEquals(1, (new KryoObjectSerializer()).encodeKey(null).length);
        assertEquals(6, (new KryoObjectSerializer()).<Object>encodeKey("object").length);
    }
}

