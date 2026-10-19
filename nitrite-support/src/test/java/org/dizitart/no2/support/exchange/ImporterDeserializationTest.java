package org.dizitart.no2.support.exchange;

import com.evil.EvilGadget;
import org.apache.commons.codec.binary.Base64;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;

import static org.dizitart.no2.support.exchange.BaseExternalTest.getRandomTempDbFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * GHSA-q86x-r2q3-vf4p: an imported exchange file must not deserialize arbitrary classes.
 */
public class ImporterDeserializationTest {

    @Test
    public void testGadgetInImportFileIsRejected() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new EvilGadget());
        }
        String json = "{\"collections\":[{\"name\":\"evil\",\"data\":[{\"key\":\""
            + Base64.encodeBase64String(bos.toByteArray()) + "\"}]}]}";

        String dbFile = getRandomTempDbFile();
        ImportOptions options = new ImportOptions();
        options.setNitriteFactory(() -> Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(dbFile).build())
            .openOrCreate());

        EvilGadget.readObjectCalled = false;
        try {
            Importer.withOptions(options).importFrom(new StringReader(json));
            fail("gadget should be rejected");
        } catch (NitriteIOException expected) {
            // rejected by the deserialization filter
        }
        assertFalse(EvilGadget.readObjectCalled);
    }
}
