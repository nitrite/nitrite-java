package org.dizitart.no2;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteSecurityTest {
    private String fileName = getRandomTempDbFile();

    @Test
    public void testSecured() throws IOException {
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
                .openOrCreate("test-user", "test-password");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
        db.close();
        Files.delete(Paths.get(fileName));
    }

    @Test
    public void testUnsecured() throws IOException {
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
                .openOrCreate();
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
        db.close();
        Files.delete(Paths.get(fileName));
    }

    @Test
    public void testInMemory() {
        Nitrite db = new NitriteBuilder()
                .compressed()
                .openOrCreate("test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        assertEquals(dbCollection.find().size(), 1);
        db.close();

        db = new NitriteBuilder()
                .compressed()
                .openOrCreate();
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 0);
        db.close();
    }
}
