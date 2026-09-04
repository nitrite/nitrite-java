/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mvstore;

import org.dizitart.no2.Nitrite;
import org.h2.mvstore.MVStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The adapter must not force MVStore's chunk retention time and versions-to-keep to 0. With both
 * at 0, H2 may reuse a chunk's blocks while the chunk map it writes at close still lists that
 * chunk, and the file then refuses to open with "Double mark" (h2database/h2database#2752,
 * #4083). A soak of a 24-thread workload with a close every 20 seconds hit that on every run at
 * 0/0 and on none with either of H2's defaults in place.
 */
public class MVStoreRetentionDefaultsTest {
    private Path directory;
    private Nitrite db;

    @Before
    public void setUp() throws Exception {
        directory = Files.createTempDirectory("nitrite-retention");
    }

    @After
    public void tearDown() throws Exception {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        try (var files = Files.walk(directory)) {
            files.sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void testDefaultsAreLeftToH2() {
        MVStoreModuleBuilder builder = MVStoreModule.withConfig();
        assertNull(builder.retentionTime());
        assertNull(builder.versionsToKeep());

        db = Nitrite.builder().loadModule(builder.filePath(directory.resolve("test.db").toFile()).build()).openOrCreate();
        MVStore store = mvStore(db);
        assertEquals(45_000, store.getRetentionTime());
        assertEquals(5, store.getVersionsToKeep());
    }

    @Test
    public void testConfiguredValuesAreApplied() {
        MVStoreModule module = MVStoreModule.withConfig()
            .filePath(directory.resolve("test.db").toFile())
            .retentionTime(1_000)
            .versionsToKeep(2)
            .build();
        db = Nitrite.builder().loadModule(module).openOrCreate();
        MVStore store = mvStore(db);
        assertEquals(1_000, store.getRetentionTime());
        assertEquals(2, store.getVersionsToKeep());
    }

    @Test
    public void testConfigCloneCarriesTheOptions() {
        MVStoreConfig config = new MVStoreConfig();
        config.retentionTime(7);
        config.versionsToKeep(3);
        MVStoreConfig clone = config.clone();
        assertEquals(Integer.valueOf(7), clone.retentionTime());
        assertEquals(Integer.valueOf(3), clone.versionsToKeep());
    }

    private static MVStore mvStore(Nitrite db) {
        try {
            var field = NitriteMVStore.class.getDeclaredField("mvStore");
            field.setAccessible(true);
            return (MVStore) field.get(db.getStore());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
