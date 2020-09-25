package org.dizitart.no2.rocksdb;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RocksDBModuleTest {
    @Test
    public void testConstructor() {
        assertTrue((new RocksDBModule("path")).getStore() instanceof RocksDBStore);
    }
}

