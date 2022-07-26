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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.handlers.*;
import org.dizitart.no2.sync.message.DataGateMessage;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MessageTemplate {
    private final Config config;
    private final ReplicatedCollection replicatedCollection;

    public MessageTemplate(Config config, ReplicatedCollection replicatedCollection) {
        this.config = config;
        this.replicatedCollection = replicatedCollection;
    }

    public void validateMessage(DataGateMessage message) {
        if (message == null) {
            throw new ReplicationException("A null message is received for "
                + replicatedCollection.getReplicaId(), true);
        } else if (message.getHeader() == null) {
            throw new ReplicationException("A message without header is received for "
                + replicatedCollection.getReplicaId(), true);
        } else if (StringUtils.isNullOrEmpty(message.getHeader().getCollection())) {
            throw new ReplicationException("A message without collection info is received for "
                + replicatedCollection.getReplicaId(), true);
        } else if (message.getHeader().getMessageType() == null) {
            throw new ReplicationException("A message without any type is received for "
                + replicatedCollection.getReplicaId(), true);
        }
    }

    public <M extends DataGateMessage> void dispatchMessage(WebSocket webSocket, M message) {
        MessageHandler<M> handler = findHandler(message);
        if (handler != null) {
            handler.handleMessage(webSocket, message);
        }
    }

    public <M extends DataGateMessage> void postMessage(WebSocket webSocket, M message) {
        try {
            String text = config.getObjectMapper().writeValueAsString(message);
            log.debug("Sending message to datagate server {}", text);
            webSocket.send(text);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("Malformed datagate message", e, true);
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends DataGateMessage> MessageHandler<M> findHandler(DataGateMessage message) {
        switch (message.getHeader().getMessageType()) {
            case Error:
                return (MessageHandler<M>) new ErrorHandler();
            case ConnectAck:
                return (MessageHandler<M>) new ConnectAckHandler(replicatedCollection);
            case Disconnect:
                return (MessageHandler<M>) new DisconnectHandler(replicatedCollection);
            case BatchChangeStart:
                return (MessageHandler<M>) new BatchChangeStartHandler(replicatedCollection);
            case BatchChangeContinue:
                return (MessageHandler<M>) new BatchChangeContinueHandler(replicatedCollection);
            case BatchChangeEnd:
                return (MessageHandler<M>) new BatchChangeEndHandler(replicatedCollection);
            case BatchAck:
                return (MessageHandler<M>) new BatchAckHandler(replicatedCollection);
            case BatchEndAck:
                return (MessageHandler<M>) new BatchEndAckHandler(replicatedCollection);
            case DataGateFeed:
                return (MessageHandler<M>) new DataGateFeedHandler(replicatedCollection);
            case DataGateFeedAck:
                return (MessageHandler<M>) new DataGateFeedAckHandler(replicatedCollection);
            default:
                break;
        }
        return null;
    }
}
