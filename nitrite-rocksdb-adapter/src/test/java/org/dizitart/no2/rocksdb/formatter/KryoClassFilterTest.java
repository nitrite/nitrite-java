package org.dizitart.no2.rocksdb.formatter;

import com.evil.KryoGadget;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * GHSA-q86x-r2q3-vf4p: kryo must not instantiate unregistered classes named in the file.
 */
public class KryoClassFilterTest {

    @Test
    public void testUnlistedClassIsRejected() {
        KryoGadget gadget = new KryoGadget();
        gadget.payload = "x";
        byte[] bytes = new KryoObjectFormatter("com.evil.**")
            .encode(Document.createDocument("field", gadget));

        KryoGadget.instantiated = false;
        try {
            new KryoObjectFormatter().decode(bytes, Document.class);
            fail("unlisted class should be rejected");
        } catch (NitriteSecurityException expected) {
            assertTrue(expected.getMessage().contains("com.evil.KryoGadget"));
        }
        assertFalse(KryoGadget.instantiated);
    }

    @Test
    public void testAllowedClassIsRead() {
        KryoGadget gadget = new KryoGadget();
        gadget.payload = "x";
        KryoObjectFormatter formatter = new KryoObjectFormatter("com.evil.KryoGadget");
        Document document = formatter.decode(formatter.encode(Document.createDocument("field", gadget)), Document.class);
        assertEquals("x", ((KryoGadget) document.get("field")).payload);
    }
}
