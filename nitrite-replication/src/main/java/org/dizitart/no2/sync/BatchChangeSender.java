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

import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.sync.crdt.ConflictFreeReplicatedDataType;
import org.dizitart.no2.sync.crdt.DeltaStates;
import org.dizitart.no2.sync.crdt.Timestamps;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class BatchChangeSender {
    private final Config config;
    private final ReplicatedCollection replicatedCollection;
    private final String replicaId;
    private final ConflictFreeReplicatedDataType replicatedDataType;
    private final DataGateSocketListener dataGateSocketListener;
    private final FeedLedger feedLedger;

    private boolean hasMore;
    private State currentState;
    private MessageFactory messageFactory;
    private Timestamps startTime;
    private Timestamps endTime;

    public BatchChangeSender(Config config,
                             ReplicatedCollection replicatedCollection,
                             DataGateSocketListener dataGateSocketListener) {
        this.config = config;
        this.replicatedCollection = replicatedCollection;
        this.replicaId = replicatedCollection.getReplicaId();
        this.feedLedger = replicatedCollection.getFeedLedger();
        this.replicatedDataType = replicatedCollection.getReplicatedDataType();
        this.dataGateSocketListener = dataGateSocketListener;
        configure();
    }

    public void sendAndReceive(WebSocket webSocket, BatchMessage batchMessage) {
        if (startTime == null) {
            startTime = replicatedDataType.getLocalSyncedTime();
        }

        if (endTime == null) {
            endTime = replicatedDataType.getLastModifiedTime();
        }

        switch (currentState) {
            case ReadyToSend:
                sendStartMessage(webSocket, batchMessage);
                break;
            case StartSent:
                sendChanges(webSocket, batchMessage);
                break;
            case ChangesSent:
                break;
        }
    }

    private void sendStartMessage(WebSocket webSocket, BatchMessage batchMessage) {
        BatchChangeStart startMessage = messageFactory.createChangeStart(config,
            replicaId, batchMessage.getHeader().getTransactionId());
        startMessage.setNextOffset(config.getChunkSize());
        startMessage.setBatchSize(config.getChunkSize());

        DeltaStates state = replicatedDataType.delta(startTime, endTime,
            0, config.getChunkSize());

        startMessage.setFeed(state);
        dataGateSocketListener.sendMessage(webSocket, startMessage);
        feedLedger.writeEntry(startMessage.getFeed());

        currentState = State.StartSent;
    }

    private void sendChanges(WebSocket webSocket, BatchMessage batchMessage) {
        DeltaStates state = replicatedDataType.delta(startTime, endTime,
            batchMessage.getNextOffset(), config.getChunkSize());

        if (state.getChangeSet().size() == 0 && state.getTombstoneMap().size() == 0) {
            hasMore = false;
        }

        if (hasMore) {
            BatchChangeContinue message = messageFactory.createChangeContinue(config,
                replicaId, batchMessage.getHeader().getTransactionId(), state);
            message.setNextOffset(batchMessage.getNextOffset() + config.getChunkSize());
            message.setBatchSize(config.getChunkSize());

            dataGateSocketListener.sendMessage(webSocket, message);
            feedLedger.writeEntry(state);
        } else {
            Receipt finalReceipt = feedLedger.getFinalReceipt();
            if (replicatedCollection.shouldRetry(finalReceipt)) {
                state = replicatedCollection.createState(finalReceipt);
                BatchChangeContinue message = messageFactory.createChangeContinue(config,
                    replicaId, batchMessage.getHeader().getTransactionId(), state);

                dataGateSocketListener.sendMessage(webSocket, message);
                feedLedger.writeEntry(state);
            } else {
                sendEndMessage(webSocket, batchMessage.getHeader().getTransactionId());
                currentState = State.ChangesSent;
            }
        }
    }

    private void sendEndMessage(WebSocket webSocket, String correlationId) {
        BatchChangeEnd endMessage = messageFactory.createChangeEnd(config,
            replicatedCollection.getReplicaId(), correlationId);

        Timestamps serverStartTime = replicatedDataType.getRemoteSyncedTime();
        endMessage.setBatchSize(config.getChunkSize());
        endMessage.setStartTime(serverStartTime);

        // end time will be passed back in batch end ack message
        endMessage.setEndTime(endTime);

        dataGateSocketListener.sendMessage(webSocket, endMessage);
    }

    private void configure() {
        this.messageFactory = new MessageFactory();
        this.hasMore = true;
        this.currentState = State.ReadyToSend;
    }

    private enum State {
        ReadyToSend,
        StartSent,
        ChangesSent
    }
}
