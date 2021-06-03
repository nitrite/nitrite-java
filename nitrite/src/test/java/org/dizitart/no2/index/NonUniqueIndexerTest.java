package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NonUniqueIndexerTest {

    @Test
    public void testConstructor() {
        NonUniqueIndexer actualNonUniqueIndexer = new NonUniqueIndexer();
        assertEquals(IndexType.NonUnique, actualNonUniqueIndexer.getIndexType());
        assertFalse(actualNonUniqueIndexer.isUnique());
    }

    @Test
    public void testIsUnique() {
        assertFalse((new NonUniqueIndexer()).isUnique());
    }

    @Test
    public void testGetIndexType() {
        assertEquals("NonUnique", (new NonUniqueIndexer()).getIndexType());
    }
}

