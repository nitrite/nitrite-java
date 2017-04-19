package org.dizitart.no2.datagate;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DbTestOperations {

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }
}
