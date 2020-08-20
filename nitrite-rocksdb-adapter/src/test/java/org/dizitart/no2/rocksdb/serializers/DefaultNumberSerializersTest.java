package org.dizitart.no2.rocksdb.serializers;

import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.junit.Test;

/**
 * @author Anindya Chatterjee
 */
public class DefaultNumberSerializersTest {
    private final KryoObjectFormatter objectFormatter = new KryoObjectFormatter();

    @Test
    public void testLongKeySerializer() {
        byte[] l1 = objectFormatter.encodeKey(10L);
        byte[] l2 = objectFormatter.encodeKey(2L);

    }
}
