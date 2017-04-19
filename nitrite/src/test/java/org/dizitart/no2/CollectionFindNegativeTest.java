package org.dizitart.no2;

import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.FindOptions.limit;
import static org.dizitart.no2.FindOptions.sort;
import static org.dizitart.no2.filters.Filters.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindNegativeTest extends BaseCollectionTest {
    @Test(expected = FilterException.class)
    public void testFindFilterInvalidAccessor() {
        insert();
        collection.find(eq("lastName.name", "ln2"));
    }

    @Test(expected = FilterException.class)
    public void testFindFilterInvalidIndex() {
        insert();
        collection.find(eq("data.9", 4));
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsNegativeOffset() {
        insert();
        collection.find(limit(-1, 1));
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsNegativeSize() {
        insert();
        collection.find(limit(0, -1));
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsInvalidOffset() {
        insert();
        collection.find(limit(10, 1));
    }

    @Test(expected = InvalidOperationException.class)
    public void testFindInvalidSort() {
        insert();
        collection.find(sort("data", SortOrder.Descending));
    }

    @Test(expected = FilterException.class)
    public void testFindTextFilterNonIndexed() {
        insert();
        collection.find(text("body", "Lorem"));
    }

    @Test(expected = FilterException.class)
    public void testFindWithRegexInvalidValue() {
        insert();
        Cursor cursor = collection.find(regex("birthDay", "hello"));
        assertEquals(cursor.size(), 1);
    }
}
