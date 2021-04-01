package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexOptionsTest {
    @Test
    public void testIndexOptions() {
        IndexOptions actualIndexOptionsResult = IndexOptions.indexOptions("indexType");
        assertEquals("indexType", actualIndexOptionsResult.getIndexType());
    }
}

