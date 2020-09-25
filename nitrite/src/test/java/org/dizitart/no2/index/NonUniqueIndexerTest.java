package org.dizitart.no2.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class NonUniqueIndexerTest {
    @Test
    public void testIsUnique() {
        assertFalse((new NonUniqueIndexer()).isUnique());
    }

    @Test
    public void testGetIndexType() {
        assertEquals("NonUnique", (new NonUniqueIndexer()).getIndexType());
    }
}

