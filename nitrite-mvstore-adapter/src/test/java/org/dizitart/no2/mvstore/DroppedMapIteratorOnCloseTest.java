package org.dizitart.no2.mvstore;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteMap;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.junit.Assert.assertThrows;

/**
 * An iterator opened on a map wrapper that was already dropped holds an MVStore version no map
 * close releases. The store must release it itself, or its close fails H2's
 * {@code oldestVersionToKeep == currentVersion} assertion.
 */
public class DroppedMapIteratorOnCloseTest {
    private Path directory;

    @After
    public void tearDown() throws Exception {
        try (var files = Files.walk(directory)) {
            files.sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void testIteratorOpenedOnDroppedMapDoesNotOutliveClose() throws Exception {
        directory = Files.createTempDirectory("nitrite-dropped-iterator");
        Nitrite db = Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(directory.resolve("t.db").toFile()).build())
            .openOrCreate();
        NitriteMap<Object, Object> map = db.getStore().openMap("test", Object.class, Object.class);
        map.put("a", 1);
        db.commit();
        map.drop();

        Iterator<?> abandoned = map.entries().iterator();
        db.getStore().openMap("other", Object.class, Object.class).put("b", 2);
        db.commit();

        db.close();
        assertThrows(NitriteIOException.class, abandoned::hasNext);
    }
}
