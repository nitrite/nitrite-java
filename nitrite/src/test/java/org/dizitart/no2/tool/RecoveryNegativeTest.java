package org.dizitart.no2.tool;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.tool.Recovery.recover;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee.
 */
public class RecoveryNegativeTest {
    private static final String fileName = getRandomTempDbFile();

    @Test(expected = IllegalStateException.class)
    public void testRecoverInvalid() throws IOException {
        File invalidDb = new File(fileName);
        if (invalidDb.createNewFile()) {
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.setLength(raf.length() + 100);
            raf.close();

            recover(fileName);
        } else {
            fail("failed to create file");
        }
    }

    @After
    public void cleanUp() throws IOException {
        Files.delete(Paths.get(fileName));
    }
}
