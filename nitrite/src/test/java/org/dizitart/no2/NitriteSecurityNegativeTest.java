package org.dizitart.no2;

import org.dizitart.no2.exceptions.NitriteException;
import org.junit.Test;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteSecurityNegativeTest {
    private String fileName = getRandomTempDbFile();

    @Test(expected = NitriteException.class)
    public void testOpenSecuredWithoutCredential() {
        Nitrite db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }

    @Test(expected = NitriteException.class)
    public void testOpenUnsecuredWithCredential() {
        Nitrite db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }

    @Test(expected = NitriteException.class)
    public void testWrongCredential() {
        Nitrite db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password2");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }
}
