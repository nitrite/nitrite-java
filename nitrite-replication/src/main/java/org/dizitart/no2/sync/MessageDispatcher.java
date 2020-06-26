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
import okhttp3.Response;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventType;
import org.dizitart.no2.sync.handlers.*;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.net.DataGateSocketListener;

import java.util.concurrent.ExecutorService;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class MessageDispatcher implements DataGateSocketListener, AutoCloseable {
    private ReplicationTemplate replicationTemplate;
    private MessageTransformer transformer;
    private ExecutorService executorService;

    public MessageDispatcher(Config config, ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
        this.transformer = new MessageTransformer(config.getObjectMapper());
    }

    @Override
    public void onMessage(String text) {
        try {
            log.debug("Message received from server {}", text);
            DataGateMessage message = transformer.transform(text);
            validateMessage(message);
            dispatch(message);
        } catch (Exception e) {
            log.error("Error while processing message", e);
            replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
            replicationTemplate.stopReplication("Error - " + e.getMessage());
        }
    }

    @Override
    public void onFailure(Throwable t, Response response) {
        log.error("Communication failure", t);
        replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, t));
        replicationTemplate.stopReplication("Error - " + t.getMessage());
    }

    @Override
    public void onClosed(int code, String reason) {
        log.warn("Connection to server is closed due to {}", reason);
    }

    private <M extends DataGateMessage> void dispatch(M message) {
        MessageHandler<M> handler = findHandler(message);
        if (handler != null) {
            getExecutorService().submit(() -> {
                try {
                    handler.handleMessage(message);
                } catch (ReplicationException error) {
                    log.error("Error occurred while handling {} message", message.getHeader().getMessageType(), error);
                    if (error.isFatal()) {
                        replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, error));
                        replicationTemplate.stopReplication("Error - " + error.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error occurred while handling {} message", message.getHeader().getMessageType(), e);
                    replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
                    replicationTemplate.stopReplication("Error - " + e.getMessage());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends DataGateMessage> MessageHandler<M> findHandler(DataGateMessage message) {
        switch (message.getHeader().getMessageType()) {
            case Error:
                return (MessageHandler<M>) new ErrorHandler(replicationTemplate);
            case Connect:
                // impossible case, server will never initiate connection
                break;
            case ConnectAck:
                return (MessageHandler<M>) new ConnectAckHandler(replicationTemplate);
            case Disconnect:
                return (MessageHandler<M>) new DisconnectHandler(replicationTemplate);
            case BatchChangeStart:
                return (MessageHandler<M>) new BatchChangeStartHandler(replicationTemplate);
            case BatchChangeContinue:
                return (MessageHandler<M>) new BatchChangeContinueHandler(replicationTemplate);
            case BatchChangeEnd:
                return (MessageHandler<M>) new BatchChangeEndHandler(replicationTemplate);
            case BatchAck:
                return (MessageHandler<M>) new BatchAckHandler(replicationTemplate);
            case BatchEndAck:
                return (MessageHandler<M>) new BatchEndAckHandler(replicationTemplate);
            case DataGateFeed:
                if (replicationTemplate.shouldExchangeFeed()) {
                    return (MessageHandler<M>) new DataGateFeedHandler(replicationTemplate);
                }
                break;
            case DataGateFeedAck:
                if (replicationTemplate.shouldExchangeFeed()) {
                    return (MessageHandler<M>) new DataGateFeedAckHandler(replicationTemplate);
                }
                break;
        }
        return null;
    }

    private void validateMessage(DataGateMessage message) {
        if (message == null) {
            throw new ReplicationException("a null message is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (message.getHeader() == null) {
            throw new ReplicationException("a message without header is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (StringUtils.isNullOrEmpty(message.getHeader().getCollection())) {
            throw new ReplicationException("a message without collection info is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (message.getHeader().getMessageType() == null) {
            throw new ReplicationException("a message without any type is received for "
                + replicationTemplate.getReplicaId(), true);
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null
            || executorService.isShutdown()
            || executorService.isTerminated()) {
            int core = Runtime.getRuntime().availableProcessors();
            executorService = ThreadPoolManager.getThreadPool(core, Constants.SYNC_THREAD_NAME);
        }
        return executorService;
    }

    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
