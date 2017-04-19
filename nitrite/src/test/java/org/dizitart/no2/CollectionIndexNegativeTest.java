package org.dizitart.no2;

import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionIndexNegativeTest extends BaseCollectionTest {
    @Test(expected = UniqueConstraintException.class)
    public void testCreateInvalidUniqueIndex() {
        collection.createIndex("lastName", IndexOptions.indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("lastName"));
        insert();
    }

    @Test(expected = IndexingException.class)
    public void testCreateIndexOnArray() {
        collection.createIndex("data", IndexOptions.indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("data"));
        insert();
    }

    @Test
    public void testCreateOnInvalidField() {
        insert();
        collection.createIndex("my-value", IndexOptions.indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("my-value"));
    }

    @Test(expected = IndexingException.class)
    public void testCreateFullTextOnNonTextField() {
        insert();
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Fulltext));
        assertTrue(collection.hasIndex("birthDay"));
    }

    @Test(expected = IndexingException.class)
    public void testDropIndexOnNonIndexedField() {
        collection.dropIndex("data");
    }

    @Test(expected = IndexingException.class)
    public void testRebuildIndexInvalid() {
        collection.rebuildIndex("unknown", true);
    }
}
