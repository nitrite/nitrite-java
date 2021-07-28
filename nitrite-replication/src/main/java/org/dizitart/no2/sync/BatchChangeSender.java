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
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class BatchChangeSender {
    private final Config config;
    private final ReplicatedCollection replicatedCollection;
    private final DataGateClient dataGateClient;

    private boolean hasMore;
    private int offset;
    private State currentState;
    private FeedLedger feedLedger;
    private MessageFactory messageFactory;
    private Long lastSyncTime;
    private Long endTime;

    public BatchChangeSender(Config config,
                             ReplicatedCollection replicatedCollection,
                             DataGateClient dataGateClient) {
        this.config = config;
        this.replicatedCollection = replicatedCollection;
        this.dataGateClient = dataGateClient;
        configure();
    }

    public void sendAndReceive(WebSocket webSocket, String correlationId, Long checkpoint) {
        if (endTime == null) {
            endTime = checkpoint;
        }

        switch (currentState) {
            case ReadyToSend:
                sendStartMessage(webSocket, correlationId);
                break;
            case StartSent:
                sendChanges(webSocket, correlationId);
                break;
            case ChangesSent:
                break;
        }
    }

    private void sendStartMessage(WebSocket webSocket, String correlationId) {
        BatchChangeStart startMessage = messageFactory.createChangeStart(config,
            replicatedCollection.getReplicaId(), correlationId);
        startMessage.setStartTime(lastSyncTime);
        startMessage.setEndTime(endTime);

        LastWriteWinState state = replicatedCollection.getChangesSince(lastSyncTime, endTime,
            0, config.getChunkSize());

        startMessage.setFeed(state);
        dataGateClient.sendMessage(webSocket, startMessage);
        feedLedger.writeEntry(startMessage.getFeed());

        offset += config.getChunkSize();
        currentState = State.StartSent;
    }

    private void sendChanges(WebSocket webSocket, String correlationId) {
        LastWriteWinState state = replicatedCollection.getChangesSince(lastSyncTime, endTime,
            offset, config.getChunkSize());

        if (state.getChangeSet().size() == 0 && state.getTombstoneMap().size() == 0) {
            hasMore = false;
        }

        if (hasMore) {
            BatchChangeContinue message = messageFactory.createChangeContinue(config,
                replicatedCollection.getReplicaId(), correlationId, state);
            message.setStartTime(lastSyncTime);
            message.setEndTime(endTime);

            dataGateClient.sendMessage(webSocket, message);
            feedLedger.writeEntry(state);
            offset += config.getChunkSize();
        } else {
            Receipt finalReceipt = replicatedCollection.getFeedLedger().getFinalReceipt();
            if (replicatedCollection.shouldRetry(finalReceipt)) {
                state = replicatedCollection.createState(finalReceipt);
                BatchChangeContinue message = messageFactory.createChangeContinue(config,
                    replicatedCollection.getReplicaId(), correlationId, state);

                message.setStartTime(lastSyncTime);
                message.setEndTime(endTime);
                dataGateClient.sendMessage(webSocket, message);
                feedLedger.writeEntry(state);
            } else {
                sendEndMessage(webSocket, correlationId);
                currentState = State.ChangesSent;
            }
        }
    }

    private void sendEndMessage(WebSocket webSocket, String correlationId) {
        BatchChangeEnd endMessage = messageFactory.createChangeEnd(config,
            replicatedCollection.getReplicaId(), correlationId);
        endMessage.setStartTime(lastSyncTime);
        endMessage.setEndTime(endTime);
        dataGateClient.sendMessage(webSocket, endMessage);
    }

    private void configure() {
        this.feedLedger = replicatedCollection.getFeedLedger();
        this.messageFactory = new MessageFactory();
        this.hasMore = true;
        this.offset = config.getChunkSize();
        this.currentState = State.ReadyToSend;
        this.lastSyncTime = replicatedCollection.getLastSyncTime();
        this.endTime = null;
    }

    private enum State {
        ReadyToSend,
        StartSent,
        ChangesSent
    }
}
