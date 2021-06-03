package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniqueIndexerTest {
    @Test
    public void testConstructor() {
        UniqueIndexer actualUniqueIndexer = new UniqueIndexer();
        assertEquals(IndexType.Unique, actualUniqueIndexer.getIndexType());
        assertTrue(actualUniqueIndexer.isUnique());
    }

    @Test
    public void testGetIndexType() {
        assertEquals("Unique", (new UniqueIndexer()).getIndexType());
    }

    @Test
    public void testIsUnique() {
        assertTrue((new UniqueIndexer()).isUnique());
    }
}

