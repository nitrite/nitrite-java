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
    public Connect createConnect(Config config, String replicaId, String txId) {
        Connect message = new Connect();
        message.setHeader(createHeader(MessageType.Connect, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        message.setAuthToken(config.getAuthToken());
        return message;
    }

    public Disconnect createDisconnect(Config config, String replicaId, String txId) {
        Disconnect message = new Disconnect();
        message.setHeader(createHeader(MessageType.Disconnect, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        return message;
    }

    public BatchChangeStart createChangeStart(Config config, String replicaId, String txId) {
        BatchChangeStart message = new BatchChangeStart();
        message.setHeader(createHeader(MessageType.BatchChangeStart, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        message.setBatchSize(config.getChunkSize());
        return message;
    }

    public BatchChangeContinue createChangeContinue(Config config, String replicaId,
                                                    String txId, LastWriteWinState state) {
        BatchChangeContinue message = new BatchChangeContinue();
        message.setHeader(createHeader(MessageType.BatchChangeContinue, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        message.setBatchSize(config.getChunkSize());
        message.setFeed(state);
        return message;
    }

    public BatchChangeEnd createChangeEnd(Config config, String replicaId,
                                          String txId) {
        BatchChangeEnd message = new BatchChangeEnd();
        message.setHeader(createHeader(MessageType.BatchChangeEnd, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        message.setBatchSize(config.getChunkSize());
        return message;
    }

    public DataGateFeed createFeedMessage(Config config, String replicaId,
                                          String txId, LastWriteWinState state) {
        DataGateFeed feed = new DataGateFeed();
        feed.setHeader(createHeader(MessageType.DataGateFeed, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        feed.setFeed(state);
        return feed;
    }

    public DataGateFeedAck createFeedAck(Config config, String replicaId,
                                         String txId, Receipt receipt) {
        DataGateFeedAck ack = new DataGateFeedAck();
        ack.setHeader(createHeader(MessageType.DataGateFeedAck, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        ack.setReceipt(receipt);
        return ack;
    }

    public BatchAck createBatchAck(Config config, String replicaId,
                                   String txId, Receipt receipt) {
        BatchAck ack = new BatchAck();
        ack.setHeader(createHeader(MessageType.BatchAck, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        ack.setReceipt(receipt);
        return ack;
    }

    public BatchEndAck createBatchEndAck(Config config, String replicaId, String txId) {
        BatchEndAck ack = new BatchEndAck();
        ack.setHeader(createHeader(MessageType.BatchEndAck, config.getCollection().getName(),
            txId, replicaId, config.getUserName(), config.getTenant()));
        return ack;
    }

    public MessageHeader createHeader(MessageType messageType, String collectionName,
                                      String txId, String replicaId,
                                      String userName, String tenant) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setTransactionId(txId);
        messageHeader.setCollection(collectionName);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(replicaId);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        messageHeader.setTenant(tenant);
        return messageHeader;
    }
}
