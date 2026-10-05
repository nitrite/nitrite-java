package org.dizitart.no2.mvstore;

import com.evil.EvilGadget;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.store.NitriteMap;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * GHSA-7w7v-2j76-mqwp: reading a database file must not deserialize classes
 * outside the allowlist.
 */
public class SerialFilterTest {
    private final String path = getRandomTempDbFile();

    @After
    public void cleanUp() {
        new File(path).delete();
    }

    private Nitrite open(String allowedClasses) {
        return Nitrite.builder().loadModule(MVStoreModule.withConfig()
            .filePath(path).allowedClasses(allowedClasses).build()).openOrCreate();
    }

    @Test
    public void rejectsGadgetInDocument() {
        try (Nitrite db = open(null)) {
            db.getCollection("notes").insert(Document.createDocument("payload", new EvilGadget()));
        }

        EvilGadget.readObjectCalled = false;
        try (Nitrite db = open(null)) {
            NitriteCollection notes = db.getCollection("notes");
            notes.find().toList();
            fail("expected the gadget to be rejected");
        } catch (Exception expected) {
            // rejected by the filter
        }
        assertFalse("gadget readObject() must not run", EvilGadget.readObjectCalled);
    }

    @Test
    public void rejectsGadgetInObjectArray() {
        try (Nitrite db = open(null)) {
            NitriteMap<String, Object> map = db.getStore().openMap("raw", String.class, Object.class);
            map.put("k", new Object[]{new EvilGadget()});
        }

        EvilGadget.readObjectCalled = false;
        try (Nitrite db = open(null)) {
            db.getStore().openMap("raw", String.class, Object.class).get("k");
            fail("expected the object array to be rejected");
        } catch (Exception expected) {
            // rejected before h2 reads its elements
        }
        assertFalse("gadget readObject() must not run", EvilGadget.readObjectCalled);
    }

    @Test
    public void allowsConfiguredClasses() {
        try (Nitrite db = open(null)) {
            db.getCollection("notes").insert(Document.createDocument("payload", new EvilGadget()));
        }

        EvilGadget.readObjectCalled = false;
        try (Nitrite db = open("com.evil.*")) {
            Document doc = db.getCollection("notes").find().firstOrNull();
            assertTrue(doc.get("payload") instanceof EvilGadget);
        }
        assertTrue(EvilGadget.readObjectCalled);
    }
}
