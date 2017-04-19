package org.dizitart.no2;

import org.dizitart.no2.exceptions.FilterException;
import org.junit.Test;

import static org.dizitart.no2.filters.Filters.text;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindByIndexNegativeTest extends BaseCollectionTest {
    @Test(expected = FilterException.class)
    public void testFindTextWithWildCardMultipleWord() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        Cursor cursor = collection.find(text("body", "*ipsum dolor*"));
        assertEquals(cursor.size(), 1);
    }

    @Test(expected = FilterException.class)
    public void testFindTextWithOnlyWildCard() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        Cursor cursor = collection.find(text("body", "*"));
        assertEquals(cursor.size(), 1);
    }
}
