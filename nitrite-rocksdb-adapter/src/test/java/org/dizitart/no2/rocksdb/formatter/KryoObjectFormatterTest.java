package org.dizitart.no2.rocksdb.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KryoObjectFormatterTest {
    @Test
    public void testEncode() {
        assertEquals(7, (new KryoObjectFormatter()).<Object>encode("object").length);
        assertEquals(1, (new KryoObjectFormatter()).<Object>encode(null).length);
    }

    @Test
    public void testEncodeKey() {
        assertEquals(1, (new KryoObjectFormatter()).<Object>encodeKey(null).length);
        assertEquals(7, (new KryoObjectFormatter()).<Object>encodeKey("object").length);
    }
}

