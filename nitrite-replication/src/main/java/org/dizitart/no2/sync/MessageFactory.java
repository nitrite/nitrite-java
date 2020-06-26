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

import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class MessageFactory {
    public Connect createConnect(Config config, String replicaId) {
        Connect message = new Connect();
        message.setHeader(createHeader(MessageType.Connect, config.getCollection().getName(),
            "", replicaId, config.getUserName()));
        message.setAuthToken(config.getAuthToken());
        return message;
    }

    public Disconnect createDisconnect(Config config, String replicaId) {
        Disconnect message = new Disconnect();
        message.setHeader(createHeader(MessageType.Disconnect, config.getCollection().getName(),
            "", replicaId, config.getUserName()));
        return message;
    }

    public BatchChangeStart createChangeStart(Config config, String replicaId, String uuid) {
        BatchChangeStart message = new BatchChangeStart();
        message.setHeader(createHeader(MessageType.BatchChangeStart, config.getCollection().getName(),
            uuid, replicaId, config.getUserName()));
        message.setBatchSize(config.getChunkSize());
        message.setDebounce(config.getDebounce());
        return message;
    }

    public BatchChangeContinue createChangeContinue(Config config, String replicaId,
                                                    String uuid, LastWriteWinState state) {
        BatchChangeContinue message = new BatchChangeContinue();
        message.setHeader(createHeader(MessageType.BatchChangeContinue, config.getCollection().getName(),
            uuid, replicaId, config.getUserName()));
        message.setBatchSize(config.getChunkSize());
        message.setDebounce(config.getDebounce());
        message.setFeed(state);
        return message;
    }

    public BatchChangeEnd createChangeEnd(Config config, String replicaId, String uuid, Long lastSyncTime) {
        BatchChangeEnd message = new BatchChangeEnd();
        message.setHeader(createHeader(MessageType.BatchChangeEnd, config.getCollection().getName(),
            uuid, replicaId, config.getUserName()));
        message.setBatchSize(config.getChunkSize());
        message.setDebounce(config.getDebounce());
        message.setLastSynced(lastSyncTime);
        return message;
    }

    public DataGateFeed createFeedMessage(Config config, String replicaId, LastWriteWinState state) {
        DataGateFeed feed = new DataGateFeed();
        feed.setHeader(createHeader(MessageType.DataGateFeed, config.getCollection().getName(),
            "", replicaId, config.getUserName()));
        feed.setFeed(state);
        return feed;
    }

    public DataGateFeedAck createFeedAck(Config config, String replicaId,
                                         String correlationId, Receipt receipt) {
        DataGateFeedAck ack = new DataGateFeedAck();
        ack.setHeader(createHeader(MessageType.DataGateFeedAck, config.getCollection().getName(),
            correlationId, replicaId, config.getUserName()));
        ack.setReceipt(receipt);
        return ack;
    }

    public BatchAck createBatchAck(Config config, String replicaId,
                                   String correlationId, Receipt receipt) {
        BatchAck ack = new BatchAck();
        ack.setHeader(createHeader(MessageType.BatchAck, config.getCollection().getName(),
            correlationId, replicaId, config.getUserName()));
        ack.setReceipt(receipt);
        return ack;
    }

    public BatchEndAck createBatchEndAck(Config config, String replicaId, String correlationId) {
        BatchEndAck ack = new BatchEndAck();
        ack.setHeader(createHeader(MessageType.BatchEndAck, config.getCollection().getName(),
            correlationId, replicaId, config.getUserName()));
        return ack;
    }

    public MessageHeader createHeader(MessageType messageType, String collectionName,
                                      String correlationId, String replicaId, String userName) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setCorrelationId(correlationId);
        messageHeader.setCollection(collectionName);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(replicaId);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }
}
