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
     * Runs the same update workload with compaction on and with it off, and holds that the file
     * is reclaimed in the first case and not in the second.
     *
     * <p><b>Only the close is asserted, deliberately.</b> {@code close(-1)} compacts
     * synchronously, so it owes nothing to a background thread being scheduled. The background
     * half of the fix - dropping {@code autoCompactFillRate(0)}, which is what keeps the file
     * bounded <i>while</i> a long-running application is writing - is not asserted here, because
     * its effect is not observable on a CI runner. Locally the chunk fill rate settles at 37%
     * against 18-21% with compaction off; on the GitHub macOS and Windows runners the two
     * configurations come out within noise of each other and in either order (16 against 18, 31
     * against 34), so both a fixed floor and a ratio between the halves failed there while the
     * fix was present and working. Guarding it would mean guarding something the runner cannot
     * see, which is how this test turned {@code main} red twice.
     */
    @Test(timeout = 180_000)
    public void testRepeatedUpdatesDoNotStrandObsoleteChunks() {
        final Outcome reclaimed = runUpdates(true);
        final Outcome unreclaimed = runUpdates(false);

        assertTrue(String.format(
                "file is %d bytes after close against %d bytes of the same live data, after %d "
                    + "updates - it was not compacted",
                reclaimed.finalFileSize, reclaimed.initialFileSize,
                2 * UPDATES_PER_ROUND * LIVE_DOCUMENTS),
            reclaimed.finalFileSize <= reclaimed.initialFileSize);

        // The control, and it has to earn the name: with compaction off the same workload must
        // leave the file larger than its live data. If it does not, something reclaimed it
        // anyway and the assertion above is comparing a thing against itself.
        assertTrue(String.format(
                "file is %d bytes after close with compaction off, against %d bytes of live "
                    + "data - nothing was left to reclaim, so this proves nothing",
                unreclaimed.finalFileSize, unreclaimed.initialFileSize),
            unreclaimed.finalFileSize > unreclaimed.initialFileSize);
    }

    private Outcome runUpdates(final boolean autoCompact) {
        final String dbPath = getRandomTempDbFile();
        final File dbFile = new File(dbPath);
        final long initialFileSize;

        final MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compress(true)
            .autoCompact(autoCompact)
            .build();

        try (final Nitrite db = Nitrite.builder()
                .loadModule(storeModule)
                .fieldSeparator(".")
                .openOrCreate()) {

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

            // Two rounds, because the first is what fills the chunks and the second is what
            // shows whether anything is reclaiming them.
            updateEveryDocument(collection);
            db.commit();
            updateEveryDocument(collection);
            db.commit();
        }

        final long finalFileSize = dbFile.length();
        deleteDb(dbPath);
        return new Outcome(initialFileSize, finalFileSize);
    }

    private static final class Outcome {
        private final long initialFileSize;
        private final long finalFileSize;

        private Outcome(final long initialFileSize, final long finalFileSize) {
            this.initialFileSize = initialFileSize;
            this.finalFileSize = finalFileSize;
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
