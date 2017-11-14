package org.dizitart.no2;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.Collection;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.filters.Filters.ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Anindya Chatterjee
 */
public class CollectionJoinTest extends BaseCollectionTest {
    private NitriteCollection foreignCollection;

    @Before
    @Override
    public void setUp() throws ParseException {
        super.setUp();
        foreignCollection = db.getCollection("foreign");
        foreignCollection.remove(ALL);

        Document fdoc1 = createDocument("fName", "fn1")
                .put("address", "ABCD Street")
                .put("telephone", "123456789");

        Document fdoc2 = createDocument("fName", "fn2")
                .put("address", "XYZ Street")
                .put("telephone", "000000000");

        Document fdoc3 = createDocument("fName", "fn2")
                .put("address", "Some other Street")
                .put("telephone", "7893141321");

        foreignCollection.insert(fdoc1, fdoc2, fdoc3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJoinAll() {
        insert();

        Lookup lookup = new Lookup();
        lookup.setLocalField("firstName");
        lookup.setForeignField("fName");
        lookup.setTargetField("personalDetails");

        RecordIterable<Document> result = collection.find().join(foreignCollection.find(), lookup);
        assertEquals(result.size(), 3);

        for (Document document : result) {
            if (document.get("firstName") == "fn1") {
                Collection<Document> personalDetails = (Collection<Document>) document.get("personalDetails");
                assertNotNull(personalDetails);
                assertEquals(personalDetails.size(), 1);
                Object[] details = personalDetails.toArray();
                assertEquals(((Document) details[0]).get("telephone"), "123456789");
            } else if (document.get("firstName") == "fn2") {
                Collection<Document> personalDetails = (Collection<Document>) document.get("personalDetails");
                assertNotNull(personalDetails);
                assertEquals(personalDetails.size(), 2);
                Object[] details = personalDetails.toArray();
                for (Object o : details) {
                    Document d = (Document) o;
                    if (d.get("address").equals("XYZ Street")) {
                        assertEquals(d.get("telephone"), "000000000");
                    } else {
                        assertEquals(d.get("telephone"), "7893141321");
                    }
                }
            } else if (document.get("firstName") == "fn3") {
                assertNull(document.get("personalDetails"));
            }
            System.out.println(document);
        }
    }
}
