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
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.ConflictFreeReplicatedDataType;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.Markers;
import org.dizitart.no2.sync.event.CollectionChangeListener;
import org.dizitart.no2.sync.handlers.ReceiptLedgerAware;
import org.dizitart.no2.sync.message.BatchMessage;
import org.dizitart.no2.sync.message.Receipt;
import org.dizitart.no2.sync.net.CloseReason;
import org.dizitart.no2.sync.net.DataGateClient;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.meta.Attributes.REPLICA;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ReplicatedCollection implements ReceiptLedgerAware {
    private String replicaId;
    private AtomicBoolean stopped;

    @Getter private final Config config;
    @Getter private final NitriteCollection collection;
    @Getter private FeedLedger feedLedger;
    @Getter private DataGateSocketListener dataGateSocketListener;
    @Getter private ConflictFreeReplicatedDataType replicatedDataType;
    @Getter private BatchChangeSender batchChangeSender;

    public ReplicatedCollection(Config config) {
        this.config = config;
        this.collection = config.getCollection();
        initialize();
    }

    public void startReplication() {
        log.debug("Starting replication for {}", getReplicaId());
        DataGateClient dataGateClient = new DataGateClient(config);
        dataGateSocketListener = new DataGateSocketListener(config, this);
        batchChangeSender = new BatchChangeSender(config, this, dataGateSocketListener);
        replicatedDataType.resetCounter();
        dataGateClient.setListener(dataGateSocketListener);
    }

    public void stopReplication(WebSocket webSocket, CloseReason reason) {
        dataGateSocketListener.closeConnection(webSocket, reason);
        setStopped(true);
    }

    public void sendAndReceive(WebSocket webSocket, BatchMessage batchMessage) {
        batchChangeSender.sendAndReceive(webSocket, batchMessage);
    }

    public void collectGarbage(Long dtl) {
        if (dtl != null && dtl > 0) {
            long collectTime = System.currentTimeMillis() - dtl * 24 * 60 * 60 * 1000;

            Receipt garbage = replicatedDataType.collectGarbage(collectTime);
            feedLedger.writeOff(garbage);
        }
    }

    public void setStopped(boolean stopped) {
        if (this.stopped == null) {
            this.stopped = new AtomicBoolean();
        }
        this.stopped.set(stopped);
    }

    public boolean isStopped() {
        return stopped != null && stopped.get();
    }

    public String getReplicaId() {
        if (StringUtils.isNullOrEmpty(replicaId)) {
            Attributes attributes = collection.getAttributes();

            if (attributes == null) {
                attributes = new Attributes();
                collection.setAttributes(attributes);
            }

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

    public void setLocalNextMarkers(Markers markers) {
        replicatedDataType.setLocalNextMarkers(markers);
    }

    public void setRemoteNextMarkers(Markers markers) {
        replicatedDataType.setRemoteNextMarkers(markers);
    }

    private void initialize() {
        stopped = new AtomicBoolean(true);
        replicatedDataType = new LastWriteWinMap(config);
        feedLedger = new FeedLedger(config);
        CollectionChangeListener changeListener = new CollectionChangeListener(replicatedDataType);
        getCollection().subscribe(changeListener);
    }
}
