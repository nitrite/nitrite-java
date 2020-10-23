package org.dizitart.no2.index;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IndexDescriptorTest {
    @Test
    public void testConstructor() {
        IndexDescriptor actualIndexDescriptor = new IndexDescriptor("indexType", "field", "collectionName");
        assertEquals("indexType", actualIndexDescriptor.getIndexType());
        assertEquals("collectionName", actualIndexDescriptor.getCollectionName());
        assertEquals("field", actualIndexDescriptor.getIndexFields());
    }

    @Test
    public void testCompareTo() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("", "", "collectionName");
        assertEquals(-14, indexDescriptor.compareTo(new IndexDescriptor("indexType", "field", "collectionName")));
    }

    @Test
    public void testCompareTo2() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("indexType", "field", "collectionName");
        assertEquals(0, indexDescriptor.compareTo(new IndexDescriptor("indexType", "field", "collectionName")));
    }
}

