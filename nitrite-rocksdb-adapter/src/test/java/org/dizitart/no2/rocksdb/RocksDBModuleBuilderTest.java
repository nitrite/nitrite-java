package org.dizitart.no2.rocksdb;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RocksDBModuleBuilderTest {
    @Test
    public void testConstructor() {
        assertNull((new RocksDBModuleBuilder()).options());
    }
}

