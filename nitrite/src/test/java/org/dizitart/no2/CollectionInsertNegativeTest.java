package org.dizitart.no2;

import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionInsertNegativeTest extends BaseCollectionTest {
    @Test(expected = UniqueConstraintException.class)
    public void testMultipleInsert() {
        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);
        collection.insert(doc1);
    }
}
