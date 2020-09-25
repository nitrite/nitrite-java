package org.dizitart.no2.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UniqueIndexerTest {
    @Test
    public void testGetIndexType() {
        assertEquals("Unique", (new UniqueIndexer()).getIndexType());
    }

    @Test
    public void testIsUnique() {
        assertTrue((new UniqueIndexer()).isUnique());
    }
}

