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

package org.dizitart.no2.test.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTransformer;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
@ServerEndpoint(value = "/datagate/{user}/{collection}")
public class SimpleDataGateEndpoint {
    private ObjectMapper objectMapper;
    private Repository repository;
    private MessageFactory factory;
    private MessageTransformer transformer;

    public SimpleDataGateEndpoint() {
        objectMapper = new ObjectMapper();
        repository = Repository.getInstance();
        factory = new MessageFactory();
        transformer = new MessageTransformer(objectMapper);
    }

    @OnOpen
    public void onOpen(@PathParam("user") String user,
                       @PathParam("collection") String collection,
                       Session session) {
        log.info("DataGate server connection established");
        session.getUserProperties().put("collection", user + "@" + collection);
        session.getUserProperties().put("authorized", false);
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.warn("DataGate server closed due to {}", reason.getReasonPhrase());
        repository.getAuthorizedSessions().remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            log.info("Message received at server {}", message);
            DataGateMessage dataGateMessage = transformer.transform(message);
            if (dataGateMessage instanceof Connect) {
                Connect connect = (Connect) dataGateMessage;
                handleConnect(session, connect);
            } else if (dataGateMessage instanceof BatchChangeStart) {
                BatchChangeStart batchChangeStart = (BatchChangeStart) dataGateMessage;
                handleBatchChangeStart(session, batchChangeStart);
            } else if (dataGateMessage instanceof BatchChangeContinue) {
                BatchChangeContinue batchChangeContinue = (BatchChangeContinue) dataGateMessage;
                handleBatchChangeContinue(session, batchChangeContinue);
            } else if (dataGateMessage instanceof BatchChangeEnd) {
                BatchChangeEnd batchChangeEnd = (BatchChangeEnd) dataGateMessage;
                handleBatchChangeEnd(session, batchChangeEnd);
            } else if (dataGateMessage instanceof DataGateFeed) {
                DataGateFeed dataGateFeed = (DataGateFeed) dataGateMessage;
                handleDataGateFeed(session, dataGateFeed);
            } else if (dataGateMessage instanceof Disconnect) {
                Disconnect disconnect = (Disconnect) dataGateMessage;
                handleDisconnect(session, disconnect);
            }
        } catch (Exception e) {
            log.error("Error while handling message {}", message, e);
        }
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        log.error("Error in DataGate server", ex);

        try {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setHeader(createHeader(MessageType.Error, null, null,
                repository.getServerId(), ""));
            errorMessage.setError(ex.getMessage());
            String message = objectMapper.writeValueAsString(errorMessage);
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            throw new ReplicationException("failed to send ErrorMessage", e, false);
        }
    }

    protected void handleConnect(Session session, Connect connect) throws IOException {
        String replicaId = connect.getHeader().getOrigin();
        String userName = connect.getHeader().getUserName();
        String collection = userName + "@" + connect.getHeader().getCollection();

        if (isValidAuth(userName, connect.getAuthToken())) {
            session.getUserProperties().put("authorized", true);
            session.getUserProperties().put("collection", collection);
            session.getUserProperties().put("replica", replicaId);

            repository.getAuthorizedSessions().add(session);

            if (repository.getCollectionReplicaMap().containsKey(collection)) {
                List<String> replicas = repository.getCollectionReplicaMap().get(collection);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                repository.getCollectionReplicaMap().put(collection, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                repository.getCollectionReplicaMap().put(collection, replicas);
            }

            if (repository.getUserReplicaMap().containsKey(userName)) {
                List<String> replicas = repository.getUserReplicaMap().get(userName);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                repository.getUserReplicaMap().put(userName, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                repository.getUserReplicaMap().put(userName, replicas);
            }

            if (!repository.getReplicaStore().containsKey(collection)) {
                LastWriteWinMap replica = createCrdt(collection);
                repository.getReplicaStore().put(collection, replica);
            }

            ConnectAck ack = new ConnectAck();
            ack.setHeader(createHeader(MessageType.ConnectAck,
                connect.getHeader().getCollection(), userName,
                repository.getServerId(), connect.getHeader().getId()));
            ack.setTombstoneTtl(repository.getGcTtl());
            String message = objectMapper.writeValueAsString(ack);
            session.getBasicRemote().sendText(message);
        } else {
            session.getUserProperties().put("authorized", false);
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setError("Unauthorized");
            errorMessage.setHeader(createHeader(MessageType.Error,
                connect.getHeader().getCollection(), userName,
                repository.getServerId(), connect.getHeader().getId()));
            String message = objectMapper.writeValueAsString(errorMessage);
            session.getBasicRemote().sendText(message);
        }
    }

    protected void handleDisconnect(Session session, Disconnect connect) {
        String replicaId = connect.getHeader().getOrigin();
        String userName = connect.getHeader().getUserName();
        String collection = userName + "@" + connect.getHeader().getCollection();

        repository.getCollectionReplicaMap().get(collection).remove(replicaId);
        repository.getUserReplicaMap().get(userName).remove(replicaId);
        repository.getAuthorizedSessions().remove(session);
    }

    protected void handleDataGateFeed(Session channel, DataGateFeed feed) {
        String userName = feed.getHeader().getUserName();
        String collection = userName + "@" + feed.getHeader().getCollection();
        String replicaId = feed.getHeader().getOrigin();

        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        replica.merge(feed.getFeed());

        try {
            Long syncTime = System.currentTimeMillis();
            String ackMessage = createAck(feed.getHeader().getCollection(), userName,
                syncTime, feed.calculateReceipt(), feed.getHeader().getId());
            channel.getBasicRemote().sendText(ackMessage);

            // other peers will take this time as last sync times
            feed.getHeader().setTimestamp(syncTime);
            String message = objectMapper.writeValueAsString(feed);
            broadcast(replicaId, collection, message);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e, false);
        }
    }

    private void broadcast(String origin, String collection, String message) {
        repository.getAuthorizedSessions().stream()
            .filter(s -> collection.equals(s.getUserProperties().get("collection")))
            .filter(s -> !origin.equals(s.getUserProperties().get("replica")))
            .forEach(s -> s.getAsyncRemote().sendText(message));
    }

    protected void handleBatchChangeEnd(Session session, BatchChangeEnd batchChangeEnd) throws IOException {
        Long lastSync = batchChangeEnd.getLastSynced();
        Integer batchSize = batchChangeEnd.getBatchSize();
        Integer debounce = batchChangeEnd.getDebounce();
        String userName = batchChangeEnd.getHeader().getUserName();
        String collection = userName + "@" + batchChangeEnd.getHeader().getCollection();

        BatchEndAck ack = new BatchEndAck();
        ack.setHeader(createHeader(MessageType.BatchEndAck, batchChangeEnd.getHeader().getCollection(),
            userName, repository.getServerId(), batchChangeEnd.getHeader().getId()));

        String message = objectMapper.writeValueAsString(ack);
        session.getBasicRemote().sendText(message);

        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        sendChanges(batchChangeEnd.getHeader().getCollection(), userName, lastSync,
            batchSize, debounce, replica, session, repository.getServerId());
    }

    protected void handleBatchChangeContinue(Session session, BatchChangeContinue batchChangeContinue) {
        DataGateFeed feed = new DataGateFeed();

        String userName = batchChangeContinue.getHeader().getUserName();
        String collection = userName + "@" + batchChangeContinue.getHeader().getCollection();
        String replicaId = batchChangeContinue.getHeader().getOrigin();
        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        replica.merge(batchChangeContinue.getFeed());

        feed.setHeader(createHeader(MessageType.DataGateFeed, batchChangeContinue.getHeader().getCollection(),
            userName, replicaId, batchChangeContinue.getHeader().getId()));
        feed.setFeed(batchChangeContinue.getFeed());

        BatchAck ack = new BatchAck();
        ack.setReceipt(feed.calculateReceipt());
        ack.setHeader(createHeader(MessageType.BatchAck, batchChangeContinue.getHeader().getCollection(),
            userName, repository.getServerId(), batchChangeContinue.getHeader().getId()));

        try {
            String message = objectMapper.writeValueAsString(ack);
            session.getBasicRemote().sendText(message);

            message = objectMapper.writeValueAsString(feed);
            broadcast(replicaId, collection, message);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e, false);
        }
    }

    protected void handleBatchChangeStart(Session session, BatchChangeStart batchChangeStart) {
        log.debug("BatchChangeStart message received " + batchChangeStart);
        DataGateFeed feed = new DataGateFeed();

        String userName = batchChangeStart.getHeader().getUserName();
        String collection = userName + "@" + batchChangeStart.getHeader().getCollection();
        String replicaId = batchChangeStart.getHeader().getOrigin();
        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        replica.merge(batchChangeStart.getFeed());

        feed.setHeader(createHeader(MessageType.DataGateFeed, batchChangeStart.getHeader().getCollection(),
            userName, replicaId, batchChangeStart.getHeader().getId()));
        feed.setFeed(batchChangeStart.getFeed());

        BatchAck ack = new BatchAck();
        ack.setReceipt(feed.calculateReceipt());
        ack.setHeader(createHeader(MessageType.BatchAck, batchChangeStart.getHeader().getCollection(),
            userName, repository.getServerId(), batchChangeStart.getHeader().getId()));

        try {
            String message = objectMapper.writeValueAsString(ack);
            session.getBasicRemote().sendText(message);

            message = objectMapper.writeValueAsString(feed);
            broadcast(replicaId, collection, message);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e, false);
        }
    }

    private LastWriteWinMap createCrdt(String collection) {
        NitriteCollection nc = repository.getDb().getCollection(collection);
        NitriteMap<NitriteId, Long> nitriteMap =
            repository.getDb().getConfig().getNitriteStore().openMap(collection + "-replica");
        return new LastWriteWinMap(nc, nitriteMap);
    }

    private void sendChanges(String collection, String userName,
                             Long lastSyncTime, Integer chunkSize,
                             Integer debounce, LastWriteWinMap crdt,
                             Session channel, String replicaId) {
        try {
            try {
                String initMessage = createChangeStart(crdt, lastSyncTime, collection, userName,
                    chunkSize, debounce);
                log.info("Sending BatchChangeStart message {} from server to {}", initMessage, replicaId);
                channel.getBasicRemote().sendText(initMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeStart to " + replicaId, e);
            }

            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                boolean hasMore = true;
                int start = chunkSize;

                @Override
                public void run() {
                    LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, chunkSize);
                    if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        try {
                            String message = createChangeContinue(state, collection, userName,
                                chunkSize, debounce);
                            log.info("Sending BatchChangeContinue message {} from server to {}", message, replicaId);
                            channel.getBasicRemote().sendText(message);
                        } catch (Exception e) {
                            log.error("Error while sending BatchChangeContinue for " + replicaId, e);
                        }

                        start = start + chunkSize;
                    }

                    if (!hasMore) {
                        timer.cancel();
                    }
                }
            }, 0, debounce);

            try {
                String endMessage = createChangeEnd(collection, userName,
                    chunkSize, debounce);
                log.info("Sending BatchChangeEnd message {} from server to {}", endMessage, replicaId);
                channel.getBasicRemote().sendText(endMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeEnd for " + replicaId, e);
            }
        } catch (Exception e) {
            throw new ReplicationException("failed to send local changes message for " + replicaId, e, false);
        }
    }

    private String createChangeStart(LastWriteWinMap crdt, Long lastSyncTime,
                                     String collection, String userName,
                                     Integer chunkSize, Integer debounce) {
        try {
            BatchChangeStart message = new BatchChangeStart();
            message.setHeader(createHeader(MessageType.BatchChangeStart,
                collection, userName, repository.getServerId(), ""));
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);

            LastWriteWinState state = crdt.getChangesSince(lastSyncTime, 0, chunkSize);
            message.setFeed(state);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeStart message", e, false);
        }
    }

    private String createChangeContinue(LastWriteWinState state,
                                        String collection, String userName,
                                        Integer chunkSize, Integer debounce) {
        try {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setHeader(createHeader(MessageType.BatchChangeContinue,
                collection, userName, repository.getServerId(), ""));
            message.setFeed(state);
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeContinue message", e, false);
        }
    }

    private String createChangeEnd(String collection, String userName,
                                   Integer chunkSize, Integer debounce) {
        try {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setHeader(createHeader(MessageType.BatchChangeEnd,
                collection, userName, repository.getServerId(), ""));
            message.setLastSynced(System.currentTimeMillis());
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeEnd message", e, false);
        }
    }

    private String createAck(String collection, String userName, Long syncTime, Receipt receipt, String corrId) {
        try {
            DataGateFeedAck ack = new DataGateFeedAck();
            MessageHeader header = createHeader(MessageType.DataGateFeedAck, collection,
                userName, repository.getServerId(), corrId);
            header.setTimestamp(syncTime);
            ack.setHeader(header);
            ack.setReceipt(receipt);
            return objectMapper.writeValueAsString(ack);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create DataGateAck message", e, false);
        }
    }

    private MessageHeader createHeader(MessageType messageType, String collection,
                                       String userName, String origin, String corrId) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setCorrelationId(corrId);
        messageHeader.setCollection(collection);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(origin);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }

    private boolean isValidAuth(String userName, String authToken) {
        return repository.getUserMap().get(userName).equals(authToken);
    }
}
