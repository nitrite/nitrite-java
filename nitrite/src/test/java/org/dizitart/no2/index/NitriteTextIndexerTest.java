package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NitriteTextIndexerTest {
    @Test
    public void testConstructor() {
        assertEquals("Fulltext", (new NitriteTextIndexer()).getIndexType());
    }

    @Test
    public void testGetIndexType() {
        assertEquals("Fulltext", (new NitriteTextIndexer()).getIndexType());
    }
}

