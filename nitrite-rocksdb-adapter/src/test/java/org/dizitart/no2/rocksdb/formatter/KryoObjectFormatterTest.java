package org.dizitart.no2.rocksdb.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KryoObjectFormatterTest {

    @Test
    public void testEncode() {
        assertEquals(7, (new KryoObjectFormatter()).<Object>encode("object").length);
        assertEquals(1, (new KryoObjectFormatter()).encode(null).length);
    }

    @Test
    public void testEncodeKey() {
        assertEquals(1, (new KryoObjectFormatter()).encodeKey(null).length);
        assertEquals(7, (new KryoObjectFormatter()).<Object>encodeKey("object").length);
    }
}

