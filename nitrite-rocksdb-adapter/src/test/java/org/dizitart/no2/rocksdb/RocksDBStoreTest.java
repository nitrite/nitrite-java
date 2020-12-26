package org.dizitart.no2.rocksdb;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RocksDBStoreTest {
    @Test
    public void testConstructor() {
        assertTrue((new RocksDBStore()).isClosed());
    }
}

