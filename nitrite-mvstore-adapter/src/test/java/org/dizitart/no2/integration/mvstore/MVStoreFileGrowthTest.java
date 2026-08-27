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
 */

package org.dizitart.no2.integration.mvstore;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.dizitart.no2.index.IndexType.NON_UNIQUE;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.junit.Test;

public class MVStoreFileGrowthTest {

    @Test(timeout = 60_000) // for github issue #1284
    public void testRepeatedUpdatesReachBoundedFileGrowth() throws InterruptedException {

        final long initialFileSize, fileSizeAfterFirstUpdates, fileSizeAfterSecondUpdates, finalFileSize;

        final String dbPath = getRandomTempDbFile();
        final File dbFile = new File(dbPath);

        System.out.println("Database File lives in: " + dbFile.getAbsolutePath());

        try (final Nitrite db = createDb(dbPath)) {

            final NitriteCollection collection = db.getCollection("file-growth");
            collection.createIndex("key");
            collection.createIndex(indexOptions(NON_UNIQUE), "revision");

            System.out.println("Setting up the initial database documents...");
            for (int i = 0; i < 25; i++) {
                collection.insert(
                    createDocument("key", i)
                        .put("revision", 0)
                        .put("lastUpdated", Instant.now().toString())
                );
            }
            System.out.println("Collection '" + collection.getName() + "' now contains " + collection.size() + " elements.");

            if (db.hasUnsavedChanges()) {
                db.commit();
            }
            initialFileSize = dbFile.length();

            System.out.println("Simulating frequent updates (1)...");
            updateDocuments(collection);
            commitAndWaitForHousekeeping(db);
            fileSizeAfterFirstUpdates = dbFile.length();

            System.out.println("Simulating frequent updates (2)...");
            updateDocuments(collection);
            commitAndWaitForHousekeeping(db);
            fileSizeAfterSecondUpdates = dbFile.length();
        }

        finalFileSize = dbFile.length();

        System.out.println("Initial file size: " + initialFileSize);
        System.out.println("File size after first updates: " + fileSizeAfterFirstUpdates);
        System.out.println("File size after second updates: " + fileSizeAfterSecondUpdates);
        System.out.println("File size after close: " + finalFileSize);

        final long maxDeviationForInitial = Math.round(initialFileSize * 0.25);
        assertTrue(
            String.format("Initial file size (%d) and file size after close (%d) differ by more than the allowed 25%% (%d bytes)", initialFileSize, finalFileSize, maxDeviationForInitial),
            Math.abs(initialFileSize - finalFileSize) <= maxDeviationForInitial
        );

        final long maxDeviationForUpdates = Math.round(fileSizeAfterFirstUpdates * 0.25);
        assertTrue(
            String.format("File size after second update loop (%d) is greater than file size after first update loop (%d) and differs by more than the allowed 25%% (%d bytes)", fileSizeAfterSecondUpdates, fileSizeAfterFirstUpdates, maxDeviationForUpdates),
            fileSizeAfterSecondUpdates < fileSizeAfterFirstUpdates || Math.abs(fileSizeAfterFirstUpdates - fileSizeAfterSecondUpdates) <= maxDeviationForUpdates
        );

        assertTrue(finalFileSize < fileSizeAfterFirstUpdates);
        assertTrue(finalFileSize < fileSizeAfterSecondUpdates);
    }

    private void updateDocuments(final NitriteCollection collection) {
        for (int i = 0; i < 2_000; i++) {
            collection.find().forEach(document -> {
                document.put("lastUpdated", Instant.now().toString());
                collection.update(document);
            });
        }
    }

    private void commitAndWaitForHousekeeping(final Nitrite db) throws InterruptedException {
        if (db.hasUnsavedChanges()) {
            db.commit();
        }
        // housekeeping usually runs once every 333ms
        // 5 seconds should be more than enough time for MVStore to write the compacted store to disk
        TimeUnit.SECONDS.sleep(5);
    }
}
