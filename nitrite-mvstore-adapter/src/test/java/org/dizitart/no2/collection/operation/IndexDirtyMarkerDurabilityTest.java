/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The index dirty marker is crash-recovery state, so it has to survive a restart.
 */
public class IndexDirtyMarkerDurabilityTest {
    private static final String COLLECTION = "dirty-marker-test";
    private static final Fields FIELDS = Fields.withNames("value");

    private String filePath;
    private Nitrite db;

    @Before
    public void setUp() {
        filePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID() + ".db";
        db = openDb();

        NitriteCollection collection = db.getCollection(COLLECTION);
        collection.insert(Document.createDocument("value", 1));
        collection.createIndex("value");

        // make sure the meta map is on disk and its page is clean again, so that only a
        // later write of the marker itself can change what the file holds
        db.commit();
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    private Nitrite openDb() {
        return Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(filePath).build())
            .openOrCreate();
    }

    private boolean reopenAndReadDirtyMarker() {
        db.close();
        db = openDb();
        try (IndexManager indexManager = new IndexManager(COLLECTION, db.getConfig())) {
            return indexManager.isDirtyIndex(FIELDS);
        }
    }

    @Test
    public void testMarkerSurvivesRestartWhenIndexingStarted() {
        try (IndexManager indexManager = new IndexManager(COLLECTION, db.getConfig())) {
            indexManager.beginIndexing(FIELDS);
        }

        assertTrue("an index that was left mid-build must still read as dirty after a restart",
            reopenAndReadDirtyMarker());
    }

    @Test
    public void testMarkerIsClearedAcrossRestartWhenIndexingCompleted() {
        try (IndexManager indexManager = new IndexManager(COLLECTION, db.getConfig())) {
            indexManager.beginIndexing(FIELDS);
            indexManager.endIndexing(FIELDS);
        }

        assertFalse("a completed index must not read as dirty after a restart",
            reopenAndReadDirtyMarker());
    }
}
