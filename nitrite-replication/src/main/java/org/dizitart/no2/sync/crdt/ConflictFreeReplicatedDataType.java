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

package org.dizitart.no2.sync.crdt;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.dizitart.no2.collection.meta.Attributes.*;
import static org.dizitart.no2.common.Constants.DOC_MODIFIED;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;

/**
 * @author Anindya Chatterjee
 */
public abstract class ConflictFreeReplicatedDataType implements AutoCloseable {
    protected final NitriteCollection collection;
    protected NitriteMap<NitriteId, Long> tombstoneMap;

    public abstract void createTombstone(NitriteId nitriteId, Long deleteTime);
    public abstract void merge(DeltaStates deltaStates);
    public abstract DeltaStates delta(Timestamps startMarker, Timestamps endMarker,
                                      int offset, int size);

    protected ConflictFreeReplicatedDataType(NitriteCollection collection) {
        this.collection = collection;
        createTombstones();
    }

    public Long getTombstoneTime(NitriteId id) {
        return tombstoneMap.get(id);
    }

    public Document getDocument(NitriteId id) {
        return collection.getById(id);
    }

    public Timestamps getLastModifiedTime() {
        Timestamps lastModifiedTime = new Timestamps();

        // get last updated document from DOC_MODIFIED index and get its modified time
        Document latest = collection.find(FindOptions.orderBy(DOC_MODIFIED, SortOrder.Descending)).firstOrNull();
        lastModifiedTime.setCollectionTime(latest == null ? 0 : latest.getLastModifiedSinceEpoch());

        Attributes attributes = getTombstoneAttributes();
        lastModifiedTime.setTombstoneTime(Long.parseLong(attributes.get(LAST_MODIFIED_TIME)));

        return lastModifiedTime;
    }

    public Receipt collectGarbage(long collectTime) {
        Set<NitriteId> removeSet = new HashSet<>();
        for (Pair<NitriteId, Long> entry : tombstoneMap.entries()) {
            if (entry.getSecond() < collectTime) {
                removeSet.add(entry.getFirst());
            }
        }

        Receipt garbage = new Receipt();
        for (NitriteId nitriteId : removeSet) {
            tombstoneMap.remove(nitriteId);
            garbage.getRemoved().add(nitriteId.getIdValue());
        }

        return garbage;
    }

    @Override
    public void close() {
        // collection should not be closed as it may be used outside of replication
        // but as tombstone is only used in replication, so close tombstone.
        if (tombstoneMap != null) {
            tombstoneMap.close();
        }
    }

    public Timestamps getLocalSyncedTime() {
        return getSyncedTime(LOCAL_COLLECTION_SYNCED_TIME, LOCAL_TOMBSTONE_SYNCED_TIME);
    }

    public Timestamps getRemoteSyncedTime() {
        return getSyncedTime(REMOTE_COLLECTION_SYNCED_TIME, REMOTE_TOMBSTONE_SYNCED_TIME);
    }

    public void setLocalSyncedTime(Timestamps timestamps) {
        Attributes collectionAttributes = getCollectionAttributes();
        collectionAttributes.set(LOCAL_COLLECTION_SYNCED_TIME, Objects.toString(timestamps.getCollectionTime()));
        collection.setAttributes(collectionAttributes);

        Attributes tombstoneAttributes = getTombstoneAttributes();
        tombstoneAttributes.set(LOCAL_TOMBSTONE_SYNCED_TIME, Objects.toString(timestamps.getTombstoneTime()));
        tombstoneMap.setAttributes(tombstoneAttributes);
    }

    public void setRemoteSyncedTime(Timestamps timestamps) {
        Attributes collectionAttributes = getCollectionAttributes();
        collectionAttributes.set(REMOTE_COLLECTION_SYNCED_TIME, Objects.toString(timestamps.getCollectionTime()));
        collection.setAttributes(collectionAttributes);

        Attributes tombstoneAttributes = getTombstoneAttributes();
        tombstoneAttributes.set(REMOTE_TOMBSTONE_SYNCED_TIME, Objects.toString(timestamps.getTombstoneTime()));
        tombstoneMap.setAttributes(tombstoneAttributes);
    }

    public Attributes getCollectionAttributes() {
        Attributes attributes = collection.getAttributes();

        if (attributes == null) {
            attributes = new Attributes();
            collection.setAttributes(attributes);
        }
        return attributes;
    }

    public Attributes getTombstoneAttributes() {
        Attributes attributes = tombstoneMap.getAttributes();

        if (attributes == null) {
            attributes = new Attributes();
            tombstoneMap.setAttributes(attributes);
        }
        return attributes;
    }

    private void createTombstones() {
        NitriteStore<?> store = collection.getStore();
        Attributes collectionAttributes = getCollectionAttributes();
        String tombstoneName = getTombstoneName(collectionAttributes);
        this.tombstoneMap = store.openMap(tombstoneName, NitriteId.class, Long.class);
        collection.setAttributes(collectionAttributes);
    }

    private String getTombstoneName(Attributes attributes) {
        String tombstoneName = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(tombstoneName)) {
            tombstoneName = collection.getName() + INTERNAL_NAME_SEPARATOR + TOMBSTONE;
            attributes.set(TOMBSTONE, tombstoneName);
        }
        return tombstoneName;
    }

    private Timestamps getSyncedTime(String collectionKey, String tombstoneKey) {
        Timestamps remoteSyncedTime = new Timestamps();

        String remoteCollectionSyncedTime = getCollectionAttributes().get(collectionKey);
        if (StringUtils.isNullOrEmpty(remoteCollectionSyncedTime)) {
            remoteSyncedTime.setCollectionTime(0L);
        } else {
            remoteSyncedTime.setCollectionTime(Long.parseLong(remoteCollectionSyncedTime));
        }

        String remoteTombstoneSyncedTime = getTombstoneAttributes().get(tombstoneKey);
        if (StringUtils.isNullOrEmpty(remoteTombstoneSyncedTime)) {
            remoteSyncedTime.setTombstoneTime(0L);
        } else {
            remoteSyncedTime.setTombstoneTime(Long.parseLong(remoteTombstoneSyncedTime));
        }

        return remoteSyncedTime;
    }
}
