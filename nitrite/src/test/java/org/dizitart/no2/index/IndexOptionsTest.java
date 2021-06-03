package org.dizitart.no2.index;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexOptionsTest {
    @Test
    public void testIndexOptions() {
        assertEquals("Index Type", IndexOptions.indexOptions("Index Type").getIndexType());
    }
}

