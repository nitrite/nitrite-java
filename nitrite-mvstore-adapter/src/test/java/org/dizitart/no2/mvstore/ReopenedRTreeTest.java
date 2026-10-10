package org.dizitart.no2.mvstore;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/** H2 reads persisted R-tree keys as DefaultSpatial rather than Nitrite's MVSpatialKey. */
public class ReopenedRTreeTest {
    @Rule
    public TemporaryFolder directory = new TemporaryFolder();

    @Test
    public void testPersistedKeysCanBeQueriedUpdatedAndRemoved() {
        File path = new File(directory.getRoot(), "spatial.db");
        BoundingBox first = new BoundingBox(1, 2, 1, 2);
        BoundingBox second = new BoundingBox(10, 11, 10, 11);
        BoundingBox moved = new BoundingBox(20, 21, 20, 21);
        BoundingBox all = new BoundingBox(0, 30, 0, 30);
        NitriteId firstId = NitriteId.createId("1");
        NitriteId secondId = NitriteId.createId("2");

        try (Nitrite db = open(path)) {
            NitriteRTree<BoundingBox, BoundingBox> tree = tree(db);
            tree.add(first, firstId);
            tree.add(second, secondId);
            db.commit();
        }
        try (Nitrite db = open(path)) {
            NitriteRTree<BoundingBox, BoundingBox> tree = tree(db);
            assertIds(tree.findIntersectingKeys(all), "1", "2");
            assertIds(tree.findContainedKeys(all), "1", "2");
            tree.remove(first, firstId);
            tree.add(moved, firstId);
            assertIds(tree.findIntersectingKeys(first));
            assertIds(tree.findIntersectingKeys(moved), "1");
            tree.remove(second, secondId);
            assertEquals(1, tree.size());
            db.commit();
        }
        try (Nitrite db = open(path)) {
            assertIds(tree(db).findContainedKeys(all), "1");
        }
    }

    private static Nitrite open(File path) {
        return Nitrite.builder().loadModule(MVStoreModule.withConfig().filePath(path)
            .autoCompact(false).build()).openOrCreate();
    }

    private static NitriteRTree<BoundingBox, BoundingBox> tree(Nitrite db) {
        return db.getStore().openRTree("bounds", BoundingBox.class, BoundingBox.class);
    }

    private static void assertIds(RecordStream<NitriteId> stream, String... ids) {
        Set<NitriteId> expected = Arrays.stream(ids).map(NitriteId::createId).collect(Collectors.toSet());
        assertEquals(expected, new HashSet<>(stream.toList()));
    }
}
