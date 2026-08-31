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

package org.dizitart.no2.mvstore;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.dizitart.no2.index.IndexType.NON_UNIQUE;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Instant;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.junit.Test;

/**
 * A store that is written to far more often than it grows must not grow anyway.
 *
 * <p>Repeatedly updating the same handful of documents leaves each chunk holding one live page and
 * a great many obsolete ones. Nothing reclaims those chunks unless MVStore is allowed to compact,
 * so the file climbs without bound while the live data stays a few kilobytes - the report in
 * <a href="https://github.com/nitrite/nitrite-java/issues/1284">gh-1284</a> reached ~800MB around
 * 100 live documents.
 *
 * @author Anindya Chatterjee
 */
public class MVStoreFileGrowthTest {

    private static final int LIVE_DOCUMENTS = 25;
    private static final int UPDATES_PER_ROUND = 2_000;

    /**
     * The fill rate the reclaimed store settles at, against the ~14-18% an unreclaimed one decays
     * to over the same rounds. It is an equilibrium rather than a target: compaction runs on the
     * back of write activity, so the rate stops falling but does not climb back once idle.
     */
    private static final int MIN_CHUNK_FILL_RATE = 25;

    @Test(timeout = 120_000)
    public void testRepeatedUpdatesDoNotStrandObsoleteChunks() {
        final String dbPath = getRandomTempDbFile();
        final File dbFile = new File(dbPath);
        final long initialFileSize;
        final int chunkFillRate;

        try (final Nitrite db = createDb(dbPath)) {
            final NitriteCollection collection = db.getCollection("file-growth");
            collection.createIndex("key");
            collection.createIndex(indexOptions(NON_UNIQUE), "revision");

            for (int i = 0; i < LIVE_DOCUMENTS; i++) {
                collection.insert(createDocument("key", i)
                    .put("revision", 0)
                    .put("lastUpdated", Instant.now().toString()));
            }
            db.commit();
            initialFileSize = dbFile.length();

            // Two rounds, because the first is what fills the chunks and the second is what shows
            // whether anything is reclaiming them.
            updateEveryDocument(collection);
            db.commit();
            updateEveryDocument(collection);
            db.commit();

            chunkFillRate = ((NitriteMVStore) db.getStore()).getMvStore()
                .getFileStore().getChunksFillRate();
        }

        // close(-1) compacts synchronously, so this one is not subject to the housekeeping thread
        // getting scheduled - the file is back to its live size by the time close() returns.
        final long finalFileSize = dbFile.length();

        try {
            assertTrue(String.format(
                    "chunks are only %d%% live after %d updates - obsolete chunks are not being reclaimed",
                    chunkFillRate, 2 * UPDATES_PER_ROUND * LIVE_DOCUMENTS),
                chunkFillRate >= MIN_CHUNK_FILL_RATE);

            assertTrue(String.format(
                    "file is %d bytes after close against %d bytes of the same live data - it was not compacted",
                    finalFileSize, initialFileSize),
                finalFileSize <= initialFileSize);
        } finally {
            deleteDb(dbPath);
        }
    }

    private void updateEveryDocument(final NitriteCollection collection) {
        for (int i = 0; i < UPDATES_PER_ROUND; i++) {
            collection.find().forEach(document -> {
                document.put("lastUpdated", Instant.now().toString());
                collection.update(document);
            });
        }
    }
}
