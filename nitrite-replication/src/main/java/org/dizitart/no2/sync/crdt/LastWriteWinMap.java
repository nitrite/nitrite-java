/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.sync.crdt;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.sync.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class LastWriteWinMap extends ConflictFreeReplicatedDataType {
    public LastWriteWinMap(Config config) {
        super(config);
    }

    @Override
    public void createTombstone(NitriteId nitriteId, Long deleteTime) {
        if (tombstones != null) {
            writeTombstoneEntry(nitriteId, deleteTime, collection.getName());
        }
    }

    @Override
    public void merge(DeltaStates deltaStates) {
        if (deltaStates.getChangeSet() != null) {
            for (Document entry : deltaStates.getChangeSet()) {
                put(entry);
            }
        }

        if (deltaStates.getTombstoneMap() != null) {
            for (Map.Entry<String, Long> entry : deltaStates.getTombstoneMap().entrySet()) {
                remove(NitriteId.createId(entry.getKey()), entry.getValue());
            }
        }
    }

    @Override
    public DeltaStates delta(Markers startMarker, Markers endMarker, int offset, int size) {
        DeltaStates deltaStates = new DeltaStates();
        deltaStates.setChangeSet(getDocumentChanges(startMarker.getCollectionMarker(), endMarker.getCollectionMarker(),
            offset, size));
        deltaStates.setTombstoneMap(getTombstoneChanges(startMarker.getTombstoneMarker(), endMarker.getTombstoneMarker(),
            offset, size));

        return deltaStates;
    }

    private Set<Document> getDocumentChanges(Long startTime, Long endTime,
                                             int offset, int size) {
        DocumentCursor cursor = collection.find(
            and(
                where(DOC_MODIFIED).gt(startTime),
                where(DOC_MODIFIED).lte(endTime)
            ), skipBy(offset).limit(size));

        return cursor.toSet();
    }

    private Map<String, Long> getTombstoneChanges(Long startTime, Long endTime,
                                                  int offset, int size) {
        DocumentCursor cursor = tombstones.find(
            and(
                where(TOMBSTONE_COUNTER).gt(startTime),
                where(TOMBSTONE_COUNTER).lte(endTime),
                where(TOMBSTONE_SOURCE).eq(collection.getName())
            ), skipBy(offset).limit(size));

        Map<String, Long> tombstoneMap = new HashMap<>();
        for (Document entry : cursor) {
            Long deletedTime = entry.get(DELETED_TIME, Long.class);
            tombstoneMap.put(entry.getId().getIdValue(), deletedTime);
        }
        return tombstoneMap;
    }

    private void put(Document value) {
        if (value != null) {
            NitriteId key = value.getId();

            Document entry = collection.getById(key);
            if (entry == null) {
                Document tombstone = tombstones.getById(key);
                if (tombstone != null) {
                    Long tombstoneTime = tombstone.get(TOMBSTONE_COUNTER, Long.class);
                    Long docModifiedTime = value.getLastModifiedSinceEpoch();

                    if (docModifiedTime >= tombstoneTime) {
                        value.put(DOC_SOURCE, REPLICATOR);
                        collection.insert(value);

                        destroyTombstone(key);
                    }
                } else {
                    value.put(DOC_SOURCE, REPLICATOR);
                    collection.insert(value);
                }
            } else {
                Integer entryRevision = entry.getRevision();
                Integer valueRevision = value.getRevision();

                if (valueRevision > entryRevision) {
                    // if the document revision is higher update it
                    entry.put(DOC_SOURCE, REPLICATOR);
                    collection.remove(entry);

                    value.put(DOC_SOURCE, REPLICATOR);
                    collection.insert(value);
                } else if (valueRevision.equals(entryRevision)) {
                    // in case for same revision number, check the last modified time
                    // if the new document is latest, update it
                    Long oldTime = entry.getLastModifiedSinceEpoch();
                    Long newTime = value.getLastModifiedSinceEpoch();

                    if (newTime > oldTime) {
                        entry.put(DOC_SOURCE, REPLICATOR);
                        collection.remove(entry);

                        value.put(DOC_SOURCE, REPLICATOR);
                        collection.insert(value);
                    }
                }
            }
        }
    }

    private void remove(NitriteId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            entry.put(DOC_SOURCE, REPLICATOR);
            collection.remove(entry);

            writeTombstoneEntry(key, timestamp, REPLICATOR);
        }
    }

    private void writeTombstoneEntry(NitriteId nitriteId, Long deleteTime, String source) {
        // to make the counter unique for each item for bulk delete
        long tombstoneCounter = REPLICATOR.equalsIgnoreCase(source)
            ? 0 : System.currentTimeMillis() + counter.incrementAndGet();
        Document tombstone = Document
            .createDocument(DOC_ID, nitriteId.getIdValue())
            .put(DELETED_TIME, deleteTime)
            .put(TOMBSTONE_COUNTER, tombstoneCounter)
            .put(TOMBSTONE_SOURCE, source);

        tombstones.insert(tombstone);
    }

    private void destroyTombstone(NitriteId nitriteId) {
        tombstones.remove(Filter.byId(nitriteId));
    }
}
