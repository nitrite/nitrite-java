package org.dizitart.no2;

import lombok.val;
import org.junit.Test;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.Document.createDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CollectionInsertTest extends BaseCollectionTest {

    @Test
    public void testInsert() {
        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        for (val document : cursor) {
            assertNotNull(document.get("firstName"));
            assertNotNull(document.get("lastName"));
            assertNotNull(document.get("birthDay"));
            assertNotNull(document.get("data"));
            assertNotNull(document.get("body"));
            assertNotNull(document.get(DOC_ID));
        }
    }

    @Test
    public void testInsertHeteroDocs() {
        Document document = createDocument("test", "Nitrite Test");

        WriteResult result = collection.insert(doc1, doc2, doc3, document);
        assertEquals(result.getAffectedCount(), 4);
    }
}
