package org.dizitart.no2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class IndexTest {

    @Test
    public void testIndexEquals() {
        Index index = new Index(IndexType.Fulltext, "test", "testColl");
        Index index2 = new Index(IndexType.Fulltext, "test", "testColl");
        assertEquals(index, index2);
    }

    @Test
    public void testIndexCompare() {
        Index index = new Index(IndexType.Fulltext, "test", "testColl");
        Index index2 = new Index(IndexType.Fulltext, "test", "testColl");

        assertEquals(index.toString(), "Index(indexType=Fulltext, field=test, collectionName=testColl)");
        assertEquals(index.compareTo(index2), 0);
    }
}
