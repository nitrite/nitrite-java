package org.dizitart.no2;

import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.ChangeType;
import org.junit.Test;

import static org.dizitart.no2.filters.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.dizitart.no2.Document.createDocument;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentMetadataTest extends BaseCollectionTest {
    @Test
    public void testTimeStamp() {
        Document document = createDocument("test_key", "test_value");
        assertEquals(document.getRevision(), 0);
        assertEquals(document.getLastModifiedTime(), 0);

        collection.insert(document);

        assertEquals(document.getRevision(), 1);
        assertTrue(document.getLastModifiedTime() > 0);

        long previous = document.getRevision();

        Cursor cursor = collection.find(eq("test_key", "test_value"));
        document = cursor.firstOrDefault();
        document.put("another_key", "another_value");

        collection.update(document);

        assertTrue(document.getRevision() > previous);

        final long time = document.getRevision();
        final Document removed = document;

        collection.register(new ChangeListener() {
            @Override
            public void onChange(ChangeInfo changeInfo) {
                if (changeInfo.getChangeType() == ChangeType.REMOVE) {
                    assertTrue(removed.getRevision() > time);
                }
            }
        });

        collection.remove(document);
    }
}
