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

package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.event.CollectionChangeListener;
import org.dizitart.no2.sync.crdt.ConflictFreeReplicatedDataType;
import org.dizitart.no2.sync.net.DataGateSocket;
import org.dizitart.no2.sync.handlers.ReceiptLedgerAware;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ReplicatedCollection implements ConflictFreeReplicatedDataType, ReceiptLedgerAware {
    private String replicaId;
    private AtomicBoolean connectedIndicator;
    private CollectionChangeListener changeListener;

    @Getter private final Config config;
    @Getter private FeedLedger feedLedger;
    @Getter private NitriteCollection collection;
    @Getter private DataGateClient dataGateClient;
    @Getter private LastWriteWinMap lastWriteWinMap;
    @Getter private BatchChangeSender batchChangeSender;

    public ReplicatedCollection(Config config) {
        this.config = config;
        configure();
    }

    public void startReplication() {
        log.debug("Starting replication for " + getReplicaId());
        DataGateSocket dataGateSocket = new DataGateSocket(config);
        dataGateClient = new DataGateClient(config, this);
        batchChangeSender = new BatchChangeSender(config, this, dataGateClient);
        dataGateSocket.setListener(dataGateClient);
    }

    public void stopReplication(WebSocket webSocket, String reason) {
        dataGateClient.closeConnection(webSocket, reason);
        reset();
    }

    public LastWriteWinState getChangesSince(Long startTime, Long endTime, int start, Integer chunkSize) {
        return lastWriteWinMap.getChangesSince(startTime, endTime, start, chunkSize);
    }

    public void sendAndReceive(WebSocket webSocket, String correlationId, Long checkpoint) {
        batchChangeSender.sendAndReceive(webSocket, correlationId, checkpoint);
    }

    public void collectGarbage(Long ttl) {
        if (ttl != null && ttl > 0) {
            long collectTime = System.currentTimeMillis() - ttl;
            if (lastWriteWinMap != null && lastWriteWinMap.getTombstoneMap() != null) {
                Set<NitriteId> removeSet = new HashSet<>();
                for (Pair<NitriteId, Long> entry : lastWriteWinMap.getTombstoneMap().entries()) {
                    if (entry.getSecond() < collectTime) {
                        removeSet.add(entry.getFirst());
                    }
                }

                Receipt garbage = new Receipt();
                for (NitriteId nitriteId : removeSet) {
                    lastWriteWinMap.getTombstoneMap().remove(nitriteId);
                    garbage.getRemoved().add(nitriteId.getIdValue());
                }

                feedLedger.writeOff(garbage);
            }
        }
    }

    public void setConnected(boolean connected) {
        this.connectedIndicator.set(connected);
    }

    public boolean isConnected() {
        return connectedIndicator.get();
    }

    public String getReplicaId() {
        if (StringUtils.isNullOrEmpty(replicaId)) {
            Attributes attributes = getAttributes();
            if (!attributes.hasKey(Attributes.REPLICA)) {
                String name = StringUtils.isNullOrEmpty(config.getReplicaName())
                    ? UUID.randomUUID().toString()
                    : config.getReplicaName() + "[" + UUID.randomUUID() + "]";
                attributes.set(REPLICA, name);
            }
            replicaId = attributes.get(Attributes.REPLICA);
        }
        return replicaId;
    }

    private void configure() {
        connectedIndicator = new AtomicBoolean(false);
        collection = config.getCollection();
        lastWriteWinMap = createConflictFreeReplicatedDataType();
        feedLedger = new FeedLedger(config, this);
        changeListener = new CollectionChangeListener(lastWriteWinMap);
        getCollection().subscribe(changeListener);
    }

    private void reset() {
        connectedIndicator = new AtomicBoolean(false);
        collection = config.getCollection();
        lastWriteWinMap = createConflictFreeReplicatedDataType();
        feedLedger = new FeedLedger(config, this);

        getCollection().unsubscribe(changeListener);
        changeListener = new CollectionChangeListener(lastWriteWinMap);
        getCollection().subscribe(changeListener);
    }
}
