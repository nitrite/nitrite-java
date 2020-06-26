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

package org.dizitart.no2.sync;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.message.Connect;
import org.dizitart.no2.sync.message.Disconnect;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;
import static org.dizitart.no2.sync.event.ReplicationEventType.Started;
import static org.dizitart.no2.sync.event.ReplicationEventType.Stopped;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Getter
public class ReplicationTemplate implements ReplicationOperation {
    private Config config;
    private MessageFactory messageFactory;
    private MessageTemplate messageTemplate;
    private LastWriteWinMap crdt;
    private FeedJournal feedJournal;

    @Getter(AccessLevel.NONE)
    private BatchChangeScheduler batchChangeScheduler;

    @Getter(AccessLevel.NONE)
    private String replicaId;

    @Getter(AccessLevel.NONE)
    private ReplicaChangeListener replicaChangeListener;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean connected;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean exchangeFlag;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean acceptCheckpoint;

    @Getter(AccessLevel.NONE)
    private ReplicationEventBus eventBus;

    public ReplicationTemplate(Config config) {
        this.config = config;
        init();
    }

    public void connect() {
        messageTemplate.openConnection();
        Connect message = messageFactory.createConnect(config, getReplicaId());
        messageTemplate.sendMessage(message);
        eventBus.post(new ReplicationEvent(Started));
    }

    public void setConnected() {
        connected.compareAndSet(false, true);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void disconnect() {
        Disconnect message = messageFactory.createDisconnect(config, getReplicaId());
        messageTemplate.sendMessage(message);
        stopReplication("User disconnect");
    }

    public void stopReplication(String reason) {
        batchChangeScheduler.stop();
        eventBus.post(new ReplicationEvent(Stopped));
        connected.set(false);
        exchangeFlag.set(false);
        acceptCheckpoint.set(false);
        messageTemplate.closeConnection(reason);
    }

    public void sendChanges() {
        batchChangeScheduler.schedule();
    }

    public void startFeedExchange() {
        this.exchangeFlag.compareAndSet(false, true);
    }

    public boolean shouldExchangeFeed() {
        return exchangeFlag.get();
    }

    public String getReplicaId() {
        if (StringUtils.isNullOrEmpty(replicaId)) {
            Attributes attributes = getAttributes();
            if (!attributes.hasKey(Attributes.REPLICA)) {
                attributes.set(REPLICA, UUID.randomUUID().toString());
            }
            replicaId = attributes.get(Attributes.REPLICA);
        }
        return replicaId;
    }

    public void setAcceptCheckpoint() {
        acceptCheckpoint.compareAndSet(false, true);
    }

    public boolean shouldAcceptCheckpoint() {
        return acceptCheckpoint.get();
    }

    @Override
    public NitriteCollection getCollection() {
        return config.getCollection();
    }

    public void subscribe(ReplicationEventListener listener) {
        eventBus.register(listener);
    }

    public void unsubscribe(ReplicationEventListener listener) {
        eventBus.deregister(listener);
    }

    public void postEvent(ReplicationEvent event) {
        eventBus.post(event);
    }

    public void close() {
        eventBus.close();
        messageTemplate.close();
        batchChangeScheduler.stop();
        this.getCollection().unsubscribe(replicaChangeListener);
    }

    public void collectGarbage(Long ttl) {
        if (ttl != null && ttl > 0) {
            long collectTime = System.currentTimeMillis() - ttl;
            if (crdt != null && crdt.getTombstones() != null) {
                Set<NitriteId> removeSet = new HashSet<>();
                for (KeyValuePair<NitriteId, Long> entry : crdt.getTombstones().entries()) {
                    if (entry.getValue() < collectTime) {
                        removeSet.add(entry.getKey());
                    }
                }

                Receipt garbage = new Receipt();
                for (NitriteId nitriteId : removeSet) {
                    crdt.getTombstones().remove(nitriteId);
                    garbage.getRemoved().add(nitriteId.getIdValue());
                }

                feedJournal.accumulate(garbage);
            }
        }
    }

    private void init() {
        this.messageFactory = new MessageFactory();
        this.connected = new AtomicBoolean(false);
        this.exchangeFlag = new AtomicBoolean(false);
        this.acceptCheckpoint = new AtomicBoolean(false);
        this.eventBus = new ReplicationEventBus();
        this.messageTemplate = new MessageTemplate(config, this);
        this.crdt = createReplicatedDataType();
        this.feedJournal = new FeedJournal(this);
        this.batchChangeScheduler = new BatchChangeScheduler(this);
        this.replicaChangeListener = new ReplicaChangeListener(this, messageTemplate);
        this.getCollection().subscribe(replicaChangeListener);
    }
}
