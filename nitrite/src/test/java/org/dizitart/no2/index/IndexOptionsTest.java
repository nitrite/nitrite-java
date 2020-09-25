package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexOptionsTest {
    @Test
    public void testIndexOptions() {
        IndexOptions actualIndexOptionsResult = IndexOptions.indexOptions("indexType", true);
        assertTrue(actualIndexOptionsResult.isAsync());
        assertEquals("indexType", actualIndexOptionsResult.getIndexType());
    }

    @Test
    public void testIndexOptions2() {
        IndexOptions actualIndexOptionsResult = IndexOptions.indexOptions("indexType");
        assertFalse(actualIndexOptionsResult.isAsync());
        assertEquals("indexType", actualIndexOptionsResult.getIndexType());
    }
}

