package org.dizitart.no2.rocksdb;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RocksDBStoreTest {
    @Test
    public void testConstructor() {
        assertTrue((new RocksDBStore()).isClosed());
    }
}

