package org.dizitart.no2;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee.
 */
public class DbWriteCloseReadTest {
    private volatile boolean writeCompleted = false;
    private final DbTestOperations readWriteOperations = new DbTestOperations();

    @Test
    public void testWriteCloseRead() throws Exception {
        try {
            readWriteOperations.createDb();
            readWriteOperations.writeCollection();
            readWriteOperations.writeIndex();
            readWriteOperations.insertInCollection();
        } catch (ParseException pe) {
            // ignore
        } finally {
            writeCompleted = true;
        }

        try {
            assertTrue(writeCompleted);
            readWriteOperations.readCollection();
        } catch (Exception e) {
            fail("collection read failed - " + e.getMessage());
        } finally {
            readWriteOperations.deleteDb();
        }
    }
}
