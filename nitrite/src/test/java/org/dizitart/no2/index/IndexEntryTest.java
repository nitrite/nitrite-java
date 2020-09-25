package org.dizitart.no2.index;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IndexEntryTest {
    @Test
    public void testConstructor() {
        IndexEntry actualIndexEntry = new IndexEntry("indexType", "field", "collectionName");
        assertEquals("indexType", actualIndexEntry.getIndexType());
        assertEquals("collectionName", actualIndexEntry.getCollectionName());
        assertEquals("field", actualIndexEntry.getField());
    }

    @Test
    public void testCompareTo() {
        IndexEntry indexEntry = new IndexEntry("", "", "collectionName");
        assertEquals(-14, indexEntry.compareTo(new IndexEntry("indexType", "field", "collectionName")));
    }

    @Test
    public void testCompareTo2() {
        IndexEntry indexEntry = new IndexEntry("indexType", "field", "collectionName");
        assertEquals(0, indexEntry.compareTo(new IndexEntry("indexType", "field", "collectionName")));
    }
}

