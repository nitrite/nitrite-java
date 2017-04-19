package org.dizitart.no2;

import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.filters.Filters.gt;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionDeleteNegativeTest extends BaseCollectionTest {
    @Test(expected = NitriteIOException.class)
    public void testDrop() {
        collection.drop();
        insert();
        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
    }

    @Test(expected = FilterException.class)
    public void testDeleteWithInvalidFilter() {
        insert();

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        WriteResult writeResult = collection.remove(gt("lastName", null));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testDeleteNullDocument() {
        insert();

        collection.remove((Document) null);
    }
}
