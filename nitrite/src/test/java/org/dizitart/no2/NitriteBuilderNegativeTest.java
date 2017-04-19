package org.dizitart.no2;

import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.junit.Test;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderNegativeTest {

    @Test(expected = NitriteIOException.class)
    public void testCreateReadonlyDatabase() {
        String filePath = getRandomTempDbFile();

        Nitrite db = Nitrite.builder()
                .readOnly()
                .filePath(filePath)
                .openOrCreate();
        db.close();
    }

    @Test(expected = InvalidOperationException.class)
    public void testCreateReadonlyInMemoryDatabase() {
        Nitrite db = Nitrite.builder()
                .readOnly()
                .openOrCreate();
        db.close();
    }

    @Test(expected = NitriteIOException.class)
    public void testOpenWithLock() {
        String filePath = getRandomTempDbFile();

        Nitrite.builder()
                .filePath(filePath)
                .openOrCreate();

        Nitrite.builder()
                .filePath(filePath)
                .openOrCreate();
    }

    @Test(expected = NitriteIOException.class)
    public void testInvalidDirectory() {
        String filePath = "/ytgr/hfurh/frij.db";
        Nitrite.builder()
                .filePath(filePath)
                .openOrCreate();
    }
}
