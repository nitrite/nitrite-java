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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.sync.Config;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.collection.meta.Attributes.*;
import static org.dizitart.no2.common.Constants.DOC_MODIFIED;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee
 */
public abstract class ConflictFreeReplicatedDataType implements AutoCloseable {
    protected static final String TOMBSTONE_COUNTER = "tombstone_counter";
    protected static final String DELETED_TIME = "deleted_time";
    protected static final String TOMBSTONE_SOURCE = "tombstone_source";

    protected final Config config;
    protected NitriteCollection collection;
    protected NitriteCollection tombstones;
    protected AtomicInteger counter;

    public abstract void createTombstone(NitriteId nitriteId, Long deleteTime);
    public abstract void merge(DeltaStates deltaStates);
    public abstract DeltaStates delta(Markers startMarker, Markers endMarker,
                                      int offset, int size);

    protected ConflictFreeReplicatedDataType(Config config) {
        this.config = config;
        initializeDataType();
    }

    public void resetCounter() {
        this.counter = new AtomicInteger(0);
    }

    public Long getTombstoneTime(NitriteId id) {
        Document tombstone = tombstones.getById(id);
        if (tombstone == null) return 0L;
        return tombstone.get(DELETED_TIME, Long.class);
    }

    public Document getDocument(NitriteId id) {
        return collection.getById(id);
    }

    public Markers getLocalEndMarkers() {
        Markers markers = new Markers();

        // get last updated document from DOC_MODIFIED index and get its modified time
        Document latest = collection.find(orderBy(DOC_MODIFIED, SortOrder.Descending)).firstOrNull();
        markers.setCollectionMarker(latest == null ? 0 : latest.getLastModifiedSinceEpoch());

        // get the highest tombstone counter for the current collection
        Document latestTombstone = tombstones.find(
            where(TOMBSTONE_SOURCE).eq(collection.getName()),
            orderBy(TOMBSTONE_COUNTER, SortOrder.Descending)).firstOrNull();

        markers.setTombstoneMarker(latestTombstone == null ? 0L : latestTombstone.get(TOMBSTONE_COUNTER, Long.class));

        return markers;
    }

    public Receipt collectGarbage(long collectTime) {
        Set<NitriteId> removeSet = new HashSet<>();
        for (Document entry : tombstones.find()) {
            if (entry.get(TOMBSTONE_COUNTER, Long.class) < collectTime) {
                removeSet.add(entry.getId());
            }
        }

        Receipt garbage = new Receipt();
        for (NitriteId nitriteId : removeSet) {
            tombstones.remove(Filter.byId(nitriteId));
            garbage.getRemoved().add(nitriteId.getIdValue());
        }

        return garbage;
    }

    @Override
    public void close() {
        // collection should not be closed as it may be used outside of replication
        // but as tombstone is only used in replication, so close tombstone.
        if (tombstones != null) {
            tombstones.close();
        }
    }

    public Markers getLocalStartMarkers() {
        return getMarkers(LOCAL_COLLECTION_MARKER, LOCAL_TOMBSTONE_MARKER);
    }

    public void setLocalNextMarkers(Markers markers) {
        setMarkers(LOCAL_COLLECTION_MARKER, LOCAL_TOMBSTONE_MARKER, markers);
    }

    public Markers getRemoteStartMarkers() {
        return getMarkers(REMOTE_COLLECTION_MARKER, REMOTE_TOMBSTONE_MARKER);
    }

    public void setRemoteNextMarkers(Markers markers) {
        setMarkers(REMOTE_COLLECTION_MARKER, REMOTE_TOMBSTONE_MARKER, markers);
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
        Attributes attributes = tombstones.getAttributes();

        if (attributes == null) {
            attributes = new Attributes();
            tombstones.setAttributes(attributes);
        }
        return attributes;
    }

    private void initializeDataType() {
        this.collection = config.getCollection();
        this.counter = new AtomicInteger(0);

        Nitrite db = config.getDb();
        Attributes collectionAttributes = getCollectionAttributes();
        String tombstoneName = getTombstoneName(collectionAttributes);

        this.tombstones = db.getCollection(tombstoneName);
        ensureIndices();
    }

    private String getTombstoneName(Attributes attributes) {
        String tombstoneName = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(tombstoneName)) {
            tombstoneName = collection.getName() + "_" + TOMBSTONE;
            attributes.set(TOMBSTONE, tombstoneName);
        }
        return tombstoneName;
    }

    private Markers getMarkers(String collectionKey, String tombstoneKey) {
        Markers markers = new Markers();

        String collectionMarker = getCollectionAttributes().get(collectionKey);
        if (StringUtils.isNullOrEmpty(collectionMarker)) {
            markers.setCollectionMarker(0L);
        } else {
            markers.setCollectionMarker(Long.parseLong(collectionMarker));
        }

        String tombstoneMarker = getTombstoneAttributes().get(tombstoneKey);
        if (StringUtils.isNullOrEmpty(tombstoneMarker)) {
            markers.setTombstoneMarker(0L);
        } else {
            markers.setTombstoneMarker(Long.parseLong(tombstoneMarker));
        }

        return markers;
    }

    private void setMarkers(String collectionKey, String tombstoneKey, Markers markers) {
        Attributes collectionAttributes = getCollectionAttributes();
        collectionAttributes.set(collectionKey, Objects.toString(markers.getCollectionMarker()));
        collection.setAttributes(collectionAttributes);

        Attributes tombstoneAttributes = getTombstoneAttributes();
        tombstoneAttributes.set(tombstoneKey, Objects.toString(markers.getTombstoneMarker()));
        tombstones.setAttributes(tombstoneAttributes);
    }

    private void ensureIndices() {
        if (!collection.hasIndex(DOC_MODIFIED)) {
            collection.createIndex(indexOptions(IndexType.NON_UNIQUE), DOC_MODIFIED);
        }

        if (!tombstones.hasIndex(TOMBSTONE_COUNTER)) {
            tombstones.createIndex(indexOptions(IndexType.NON_UNIQUE), TOMBSTONE_COUNTER);
        }
    }
}
