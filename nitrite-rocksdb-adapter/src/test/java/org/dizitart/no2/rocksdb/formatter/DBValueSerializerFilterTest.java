package org.dizitart.no2.rocksdb.formatter;

import com.evil.SerialGadget;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * GHSA-463g-rx95-c9fp: DBValue/DBNull index keys must not natively deserialize unlisted classes.
 */
public class DBValueSerializerFilterTest {

    @Test
    public void testGadgetInIndexKeyIsRejected() {
        KryoObjectFormatter formatter = new KryoObjectFormatter();
        byte[] bytes = formatter.encodeKey(new DBValue(new SerialGadget()));

        SerialGadget.deserialized = false;
        try {
            formatter.decodeKey(bytes, DBValue.class);
            fail("unlisted class should be rejected");
        } catch (NitriteIOException expected) {
            // rejected before readObject ran
        }
        assertFalse(SerialGadget.deserialized);
    }

    @Test
    public void testLegitKeysRoundTrip() {
        KryoObjectFormatter formatter = new KryoObjectFormatter();
        DBValue value = new DBValue("abc");
        assertEquals(value, formatter.decodeKey(formatter.encodeKey(value), DBValue.class));
        DBNull nul = DBNull.getInstance();
        assertNotNull(formatter.decodeKey(formatter.encodeKey(nul), DBNull.class));
    }
}
