package org.dizitart.no2.rocksdb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RocksDBConfigTest {
    @Test
    public void testConstructor() {
        RocksDBConfig actualRocksDBConfig = new RocksDBConfig();
        assertFalse(actualRocksDBConfig.isInMemory());
        assertNull(actualRocksDBConfig.options());
    }
}

