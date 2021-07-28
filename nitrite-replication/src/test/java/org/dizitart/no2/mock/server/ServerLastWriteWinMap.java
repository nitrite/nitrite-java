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

package org.dizitart.no2.mock.server;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.crdt.Tombstone;

import java.util.Map;

import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;

/**
 * @author Anindya Chatterjee
 */
@Data
@Slf4j
public class ServerLastWriteWinMap {
    private NitriteCollection collection;
    private NitriteMap<NitriteId, Tombstone> tombstoneMap;

    public ServerLastWriteWinMap(NitriteCollection collection, NitriteMap<NitriteId, Tombstone> tombstoneMap) {
        this.collection = collection;
        this.tombstoneMap = tombstoneMap;
    }

    public void merge(LastWriteWinState snapshot) {
        if (snapshot.getChangeSet() != null) {
            for (Document entry : snapshot.getChangeSet()) {
                put(entry);
            }
        }

        if (snapshot.getTombstoneMap() != null) {
            for (Map.Entry<String, Long> entry : snapshot.getTombstoneMap().entrySet()) {
                remove(NitriteId.createId(entry.getKey()), entry.getValue());
            }
        }

        log.debug("Server Collection after merge - " + collection.find().toList());
        log.debug("Server Tombstone after merge - " + tombstoneMap.entries().toList());
    }

    public LastWriteWinState getChangesSince(Long startTime, Long endTime, int offset, int size) {
        LastWriteWinState state = new LastWriteWinState();

        DocumentCursor cursor = collection.find(
            and(
                where("_synced").gte(startTime),
                where("_synced").lte(endTime)
            ), skipBy(offset).limit(size));

        state.getChangeSet().addAll(cursor.toSet());

        // send tombstone info in first batch only
        if (offset == 0) {
            // don't repeat for other offsets
            for (Pair<NitriteId, Tombstone> entry : tombstoneMap.entries()) {
                Long syncTimestamp = entry.getSecond().getSyncTimestamp();
                if (syncTimestamp > startTime && syncTimestamp <= endTime) {
                    state.getTombstoneMap().put(entry.getFirst().getIdValue(),
                        entry.getSecond().getDeleteTimestamp());
                }
            }
        }

        return state;
    }

    private void put(Document value) {
        if (value != null) {
            value.put("_synced", System.currentTimeMillis());
            NitriteId key = value.getId();

            Document entry = collection.getById(key);
            if (entry == null) {
                if (tombstoneMap.containsKey(key)) {
                    Long tombstoneTime = tombstoneMap.get(key).getDeleteTimestamp();
                    Long docModifiedTime = value.getLastModifiedSinceEpoch();

                    if (docModifiedTime >= tombstoneTime) {
                        value.put(DOC_SOURCE, REPLICATOR);
                        collection.insert(value);
                        tombstoneMap.remove(key);
                    }
                } else {
                    value.put(DOC_SOURCE, REPLICATOR);
                    collection.insert(value);
                }
            } else {
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

    private void remove(NitriteId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            entry.put(DOC_SOURCE, REPLICATOR);

            log.debug("Removing {} from collection for tombstone", key.getIdValue());
            collection.remove(entry);
            Tombstone tombstone = new Tombstone();
            tombstone.setNitriteId(key);
            tombstone.setDeleteTimestamp(timestamp);
            tombstone.setSyncTimestamp(System.currentTimeMillis());
            tombstoneMap.put(key, tombstone);
        } else {
            log.debug("No entry found to remove");
        }
    }
}
