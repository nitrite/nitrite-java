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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.streams.BoundedStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.index.IndexType;

import java.util.*;

import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.collection.meta.Attributes.LAST_MODIFIED_TIME;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class LastWriteWinMap extends ConflictFreeReplicatedDataType {

    public LastWriteWinMap(NitriteCollection collection) {
        super(collection);
        ensureIndices();
    }

    @Override
    public void createTombstone(NitriteId nitriteId, Long deleteTime) {
        if (tombstoneMap != null) {
            if (tombstoneMap.containsKey(nitriteId)) {
                Long time = tombstoneMap.get(nitriteId);
                if (deleteTime > time) {
                    writeTombstoneEntry(nitriteId, deleteTime);
                }
            } else {
                writeTombstoneEntry(nitriteId, deleteTime);
            }
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
    public DeltaStates delta(Timestamps startMarker, Timestamps endMarker, int offset, int size) {
        DeltaStates deltaStates = new DeltaStates();
        deltaStates.setChangeSet(getDocumentChanges(startMarker.getCollectionTime(), endMarker.getCollectionTime(),
            offset, size));
        deltaStates.setTombstoneMap(getTombstoneChanges(startMarker.getTombstoneTime(), endMarker.getTombstoneTime(),
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
        BoundedStream<NitriteId, Long> stream
            = new BoundedStream<>((long) offset, (long) size, tombstoneMap.entries());

        Map<String, Long> tombstoneMap = new HashMap<>();
        for (Pair<NitriteId, Long> entry : stream) {
            Long syncTimestamp = entry.getSecond();
            if (syncTimestamp > startTime && syncTimestamp <= endTime) {
                tombstoneMap.put(entry.getFirst().getIdValue(),
                    entry.getSecond());
            }
        }
        return tombstoneMap;
    }

    private void put(Document value) {
        if (value != null) {
            NitriteId key = value.getId();

            Document entry = collection.getById(key);
            if (entry == null) {
                if (tombstoneMap.containsKey(key)) {
                    Long tombstoneTime = tombstoneMap.get(key);
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

            createTombstone(key, timestamp);
        }
    }

    private void writeTombstoneEntry(NitriteId nitriteId, Long deleteTime) {
        // if current deleted time is greater than previous deleted time,
        // update the deleted time
        tombstoneMap.put(nitriteId, deleteTime);

        // update last modified date in tombstone attributes
        Attributes attributes = getTombstoneAttributes();
        long lastModifiedTime = Long.parseLong(attributes.get(LAST_MODIFIED_TIME));
        if (deleteTime > lastModifiedTime) {
            // if deleted date is higher than the already saved last modified time
            // then only update it, otherwise ignore
            attributes.set(LAST_MODIFIED_TIME, Long.toString(deleteTime));
            tombstoneMap.setAttributes(attributes);
        }
    }

    private void destroyTombstone(NitriteId nitriteId) {
        tombstoneMap.remove(nitriteId);

        // update last modified date in tombstone attributes
        Attributes attributes = getTombstoneAttributes();
        List<Long> deleteTimes = Iterables.toList(tombstoneMap.values());
        Collections.sort(deleteTimes, Collections.reverseOrder());

        long lastModifiedTime = deleteTimes.get(0);
        attributes.set(LAST_MODIFIED_TIME, Long.toString(lastModifiedTime));
        tombstoneMap.setAttributes(attributes);
    }

    private void ensureIndices() {
        if (!collection.hasIndex(DOC_MODIFIED)) {
            collection.createIndex(indexOptions(IndexType.NON_UNIQUE), DOC_MODIFIED);
        }
    }
}
