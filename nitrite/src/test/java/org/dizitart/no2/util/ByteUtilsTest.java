package org.dizitart.no2.util;

import org.junit.Test;

import static org.dizitart.no2.util.ByteUtils.bytesToLong;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ByteUtilsTest {

    @Test
    public void testBytesToLong() {
        long value = bytesToLong(new byte[]{2, 5, 9, 16, 5, 7, 5, 9});
        assertEquals(value, 145532527367881993L);

        value = bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
        assertEquals(value, 0);
    }
}
