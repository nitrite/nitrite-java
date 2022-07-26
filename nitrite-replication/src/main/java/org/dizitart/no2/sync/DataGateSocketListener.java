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

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.event.ReplicationEventType;
import org.dizitart.no2.sync.message.Connect;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.Disconnect;
import org.dizitart.no2.sync.net.CloseReason;
import org.dizitart.no2.sync.net.WebSocketCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static org.dizitart.no2.sync.event.ReplicationEventType.Started;
import static org.dizitart.no2.sync.event.ReplicationEventType.Stopped;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DataGateSocketListener extends WebSocketListener {
    private final Config config;
    private final ReplicatedCollection replicatedCollection;

    private ReplicationEventBus eventBus;
    private MessageFactory messageFactory;
    private MessageTemplate messageTemplate;
    private MessageTransformer transformer;
    private WebSocket connectedWebsocket;

    public DataGateSocketListener(Config config, ReplicatedCollection replicatedCollection) {
        this.config = config;
        this.replicatedCollection = replicatedCollection;
        configure();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        try {
            if (!StringUtils.isNullOrEmpty(config.getReplicaName())) {
                Thread.currentThread().setName(config.getReplicaName());
            }

            log.debug("Connection opened, sending Connect message");
            this.connectedWebsocket = webSocket;
            String correlationId = UUID.randomUUID().toString();
            Connect message = messageFactory.createConnect(config, replicatedCollection.getReplicaId(), correlationId);
            messageTemplate.postMessage(webSocket, message);
            eventBus.post(new ReplicationEvent(Started));
        } catch (Exception e) {
            log.error("Opening websocket failed", e);
            eventBus.post(new ReplicationEvent(ReplicationEventType.Error, e));
            closeConnection(webSocket, new CloseReason("Client Error", e));
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        try {
            log.debug("Received message from server {}", text);
            DataGateMessage message = transformer.transform(text);
            messageTemplate.validateMessage(message);
            messageTemplate.dispatchMessage(webSocket, message);
        } catch (Exception e) {
            log.error("Error while processing message", e);
            eventBus.post(new ReplicationEvent(ReplicationEventType.Error, e));

            if (e instanceof ReplicationException) {
                if (((ReplicationException) e).isFatal()) {
                    closeConnection(webSocket, new CloseReason("Client Error", e));
                }
            }
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.error("Communication failure", t);
        eventBus.post(new ReplicationEvent(ReplicationEventType.Error, t));
        closeConnection(webSocket, new CloseReason("Client Error", t));
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.warn("Connection to server is closed due to {}", reason);
        eventBus.post(new ReplicationEvent(Stopped));
        replicatedCollection.setStopped(true);
    }

    public <M extends DataGateMessage> void sendMessage(WebSocket webSocket, M message) {
        try {
            if (!replicatedCollection.isStopped()) {
                messageTemplate.postMessage(webSocket, message);
            } else {
                throw new IllegalStateException("Datagate client is not connected");
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
            eventBus.post(new ReplicationEvent(ReplicationEventType.Error, e));
            closeConnection(webSocket, new CloseReason("Client Error", e));
        }
    }

    public void closeConnection(WebSocket webSocket, CloseReason reason) {
        log.debug("Closing connection due to {}", reason);

        if (reason == CloseReason.ClientClose) {
            Disconnect disconnect = messageFactory.createDisconnect(config,
                replicatedCollection.getReplicaId(), UUID.randomUUID().toString());
            if (connectedWebsocket != null) {
                messageTemplate.postMessage(connectedWebsocket, disconnect);
            }
        }

        replicatedCollection.setStopped(true);

        if (webSocket != null) {
            webSocket.close(WebSocketCode.NORMAL_CLOSE, reason.getReasonMessage());
        } else {
            if (connectedWebsocket != null) {
                connectedWebsocket.close(WebSocketCode.NORMAL_CLOSE, reason.getReasonMessage());
            }
        }

        eventBus.post(new ReplicationEvent(Stopped));
    }

    private void configure() {
        eventBus = new ReplicationEventBus();
        messageFactory = new MessageFactory();
        messageTemplate = new MessageTemplate(config, replicatedCollection);
        transformer = new MessageTransformer(config.getObjectMapper());

        if (config.getEventListeners() != null) {
            for (ReplicationEventListener eventListener : config.getEventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
