package org.dizitart.no2.rocksdb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RocksDBReferenceTest {
    @Test
    public void testConstructor() {
        assertEquals("RocksDBReference(options=null, dbOptions=null, columnFamilyOptions=null, rocksDB=null, columnFamilyD"
                + "escriptors=[], columnFamilyHandleRegistry={}, dbComparators=[])", (new RocksDBReference()).toString());
    }
}

