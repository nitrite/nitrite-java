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

package org.dizitart.no2.mock.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.FeedLedger;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTransformer;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.crdt.Tombstone;
import org.dizitart.no2.sync.message.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
@ServerEndpoint(value = "/ws/datagate/{tenant}/{collection}/{user}")
public class MockDataGateEndpoint {
    private ObjectMapper objectMapper;
    private MockRepository mockRepository;
    private MessageFactory factory;
    private MessageTransformer transformer;

    public MockDataGateEndpoint() {
        objectMapper = new ObjectMapper();
        mockRepository = MockRepository.getInstance();
        factory = new MessageFactory();
        transformer = new MessageTransformer(objectMapper);
    }

    @OnOpen
    public void onOpen(@PathParam("user") String user,
                       @PathParam("tenant") String tenant,
                       @PathParam("collection") String collection,
                       Session session) {
        log.info("DataGate server connection established");
        session.getUserProperties().put("user", user);
        session.getUserProperties().put("tenant", tenant);
        session.getUserProperties().put("collection", user + "@" + collection);
        session.getUserProperties().put("authorized", false);
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.warn("DataGate server closed due to {}", reason.getReasonPhrase());
        mockRepository.getAuthorizedSessions().remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        try {
            log.info("Message received at server {}", message);
            DataGateMessage dataGateMessage = transformer.transform(message);
            if (dataGateMessage instanceof Connect) {
                Connect connect = (Connect) dataGateMessage;
                handleConnect(session, connect);
            } else if (dataGateMessage instanceof BatchChangeStart) {
                checkAuthorized(session);
                BatchChangeStart batchChangeStart = (BatchChangeStart) dataGateMessage;
                handleBatchChangeStart(session, batchChangeStart);
            } else if (dataGateMessage instanceof BatchChangeContinue) {
                checkAuthorized(session);
                BatchChangeContinue batchChangeContinue = (BatchChangeContinue) dataGateMessage;
                handleBatchChangeContinue(session, batchChangeContinue);
            } else if (dataGateMessage instanceof BatchChangeEnd) {
                checkAuthorized(session);
                BatchChangeEnd batchChangeEnd = (BatchChangeEnd) dataGateMessage;
                handleBatchChangeEnd(session, batchChangeEnd);
            } else if (dataGateMessage instanceof DataGateFeed) {
                checkAuthorized(session);
                DataGateFeed dataGateFeed = (DataGateFeed) dataGateMessage;
                handleDataGateFeed(session, dataGateFeed);
            } else if (dataGateMessage instanceof BatchAck) {
                checkAuthorized(session);
                BatchAck batchAck = (BatchAck) dataGateMessage;
                handleBatchAck(session, batchAck);
            } else if (dataGateMessage instanceof BatchEndAck) {
                checkAuthorized(session);
                BatchEndAck batchEndAck = (BatchEndAck) dataGateMessage;
                handleBatchEndAck(session, batchEndAck);
            } else if (dataGateMessage instanceof Disconnect) {
                checkAuthorized(session);
                Disconnect disconnect = (Disconnect) dataGateMessage;
                handleDisconnect(session, disconnect);
            }
        } catch (Exception e) {
            log.error("Error while handling message {}", message, e);
            e.printStackTrace();
            sendErrorMessage(session, e);
        }
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        log.error("Error in DataGate server", ex);

        try {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setHeader(createHeader(session, MessageType.Error, null, null,
                mockRepository.getServerId(), ""));
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

            mockRepository.getAuthorizedSessions().add(session);

            List<String> replicas;
            if (mockRepository.getCollectionReplicaMap().containsKey(collection)) {
                replicas = mockRepository.getCollectionReplicaMap().get(collection);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
            } else {
                replicas = new ArrayList<>();
                replicas.add(replicaId);
            }
            mockRepository.getCollectionReplicaMap().put(collection, replicas);

            if (mockRepository.getUserReplicaMap().containsKey(userName)) {
                replicas = mockRepository.getUserReplicaMap().get(userName);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
            } else {
                replicas = new ArrayList<>();
                replicas.add(replicaId);
            }
            mockRepository.getUserReplicaMap().put(userName, replicas);

            if (!mockRepository.getReplicaStore().containsKey(collection)) {
                ServerLastWriteWinMap replica = createCrdt(collection);
                mockRepository.getReplicaStore().put(collection, replica);
            }

            ConnectAck ack = new ConnectAck();
            ack.setHeader(createHeader(session, MessageType.ConnectAck,
                connect.getHeader().getCollection(), userName,
                mockRepository.getServerId(), connect.getHeader().getTransactionId()));
            ack.setTombstoneTtl(mockRepository.getGcTtl());
            ack.setNextOffset(0);
            ack.setBatchSize(0);

            String message = objectMapper.writeValueAsString(ack);
            session.getBasicRemote().sendText(message);
        } else {
            session.getUserProperties().put("authorized", false);
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setError("Unauthorized");
            errorMessage.setHeader(createHeader(session, MessageType.Error,
                connect.getHeader().getCollection(), userName,
                mockRepository.getServerId(), connect.getHeader().getTransactionId()));
            String message = objectMapper.writeValueAsString(errorMessage);
            session.getBasicRemote().sendText(message);
        }
    }

    protected void handleBatchChangeStart(Session session, BatchChangeStart batchChangeStart) throws IOException {
        DataGateFeed feed = new DataGateFeed();

        String userName = batchChangeStart.getHeader().getUserName();
        String collection = userName + "@" + batchChangeStart.getHeader().getCollection();
        String replicaId = batchChangeStart.getHeader().getOrigin();
        ServerLastWriteWinMap replica = mockRepository.getReplicaStore().get(collection);
        replica.merge(batchChangeStart.getFeed(), batchChangeStart.getEndTime());

        feed.setHeader(createHeader(session, MessageType.DataGateFeed, batchChangeStart.getHeader().getCollection(),
            userName, replicaId, batchChangeStart.getHeader().getTransactionId()));
        feed.setFeed(batchChangeStart.getFeed());

        BatchAck ack = new BatchAck();
        ack.setReceipt(feed.calculateReceipt());
        ack.setHeader(createHeader(session, MessageType.BatchAck, batchChangeStart.getHeader().getCollection(),
            userName, mockRepository.getServerId(), batchChangeStart.getHeader().getTransactionId()));
        ack.setStartTime(batchChangeStart.getStartTime());
        ack.setEndTime(batchChangeStart.getEndTime());
        ack.setNextOffset(batchChangeStart.getNextOffset());
        ack.setBatchSize(batchChangeStart.getBatchSize());

        String message = objectMapper.writeValueAsString(ack);
        session.getBasicRemote().sendText(message);
    }

    protected void handleBatchChangeContinue(Session session, BatchChangeContinue batchChangeContinue) throws IOException {
        DataGateFeed feed = new DataGateFeed();

        String userName = batchChangeContinue.getHeader().getUserName();
        String collection = userName + "@" + batchChangeContinue.getHeader().getCollection();
        String replicaId = batchChangeContinue.getHeader().getOrigin();
        ServerLastWriteWinMap replica = mockRepository.getReplicaStore().get(collection);
        replica.merge(batchChangeContinue.getFeed(), batchChangeContinue.getEndTime());

        feed.setHeader(createHeader(session, MessageType.DataGateFeed, batchChangeContinue.getHeader().getCollection(),
            userName, replicaId, batchChangeContinue.getHeader().getTransactionId()));
        feed.setFeed(batchChangeContinue.getFeed());

        BatchAck ack = new BatchAck();
        ack.setReceipt(feed.calculateReceipt());
        ack.setHeader(createHeader(session, MessageType.BatchAck, batchChangeContinue.getHeader().getCollection(),
            userName, mockRepository.getServerId(), batchChangeContinue.getHeader().getTransactionId()));
        ack.setStartTime(batchChangeContinue.getStartTime());
        ack.setEndTime(batchChangeContinue.getEndTime());
        ack.setNextOffset(batchChangeContinue.getNextOffset());
        ack.setBatchSize(batchChangeContinue.getBatchSize());

        String message = objectMapper.writeValueAsString(ack);
        session.getBasicRemote().sendText(message);
    }

    protected void handleBatchChangeEnd(Session session, BatchChangeEnd batchChangeEnd) throws IOException {
        Integer batchSize = batchChangeEnd.getBatchSize();
        String userName = batchChangeEnd.getHeader().getUserName();
        String collection = userName + "@" + batchChangeEnd.getHeader().getCollection();

        BatchEndAck ack = new BatchEndAck();
        ack.setHeader(createHeader(session, MessageType.BatchEndAck, batchChangeEnd.getHeader().getCollection(),
            userName, mockRepository.getServerId(), batchChangeEnd.getHeader().getTransactionId()));
        ack.setStartTime(batchChangeEnd.getStartTime());
        ack.setEndTime(batchChangeEnd.getEndTime());

        String message = objectMapper.writeValueAsString(ack);
        session.getBasicRemote().sendText(message);

        ServerLastWriteWinMap replica = mockRepository.getReplicaStore().get(collection);
        LastWriteWinState changesSince = replica.getChangesSince(batchChangeEnd.getStartTime(),
            batchChangeEnd.getEndTime(), 0, batchSize);


        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setHeader(createHeader(session, MessageType.BatchChangeStart,
            collection, userName, mockRepository.getServerId(), batchChangeEnd.getHeader().getTransactionId()));
        batchChangeStart.setStartTime(batchChangeEnd.getStartTime());
        batchChangeStart.setEndTime(batchChangeEnd.getEndTime());
        batchChangeStart.setFeed(changesSince);
        batchChangeStart.setNextOffset(batchSize);
        batchChangeStart.setBatchSize(batchSize);

        session.getBasicRemote().sendText(objectMapper.writeValueAsString(batchChangeStart));
    }

    protected void handleBatchAck(Session session, BatchAck batchAck) throws IOException {
        String userName = batchAck.getHeader().getUserName();
        String collection = userName + "@" + batchAck.getHeader().getCollection();
        Integer offset = batchAck.getNextOffset();

        Receipt receipt = batchAck.getReceipt();
        FeedLedger feedLedger = mockRepository.getFeedLedgerMap().get(collection);
        if (feedLedger != null) {
            feedLedger.writeOff(receipt);
            mockRepository.getFeedLedgerMap().put(collection, feedLedger);
        }

        ServerLastWriteWinMap replica = mockRepository.getReplicaStore().get(collection);
        LastWriteWinState changesSince = replica.getChangesSince(batchAck.getStartTime(),
            batchAck.getEndTime(), offset, batchAck.getBatchSize());

        boolean hasMore = !(changesSince.getChangeSet().size() == 0 && changesSince.getTombstoneMap().size() == 0);
        if (hasMore) {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setHeader(createHeader(session, MessageType.BatchChangeContinue,
                collection, userName, mockRepository.getServerId(), batchAck.getHeader().getTransactionId()));
            message.setFeed(changesSince);
            message.setStartTime(batchAck.getStartTime());
            message.setEndTime(batchAck.getEndTime());
            message.setNextOffset(batchAck.getNextOffset() + batchAck.getBatchSize());
            message.setBatchSize(batchAck.getBatchSize());

            session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
        } else {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setHeader(createHeader(session, MessageType.BatchChangeEnd,
                collection, userName, mockRepository.getServerId(), batchAck.getHeader().getTransactionId()));
            message.setStartTime(batchAck.getStartTime());
            message.setEndTime(batchAck.getEndTime());
            message.setNextOffset(batchAck.getNextOffset());
            message.setBatchSize(batchAck.getBatchSize());

            session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
        }
    }

    protected void handleDisconnect(Session session, Disconnect connect) {
        String replicaId = connect.getHeader().getOrigin();
        String userName = connect.getHeader().getUserName();
        String collection = userName + "@" + connect.getHeader().getCollection();

        mockRepository.getCollectionReplicaMap().get(collection).remove(replicaId);
        mockRepository.getUserReplicaMap().get(userName).remove(replicaId);
        mockRepository.getAuthorizedSessions().remove(session);
    }

    protected void handleDataGateFeed(Session channel, DataGateFeed feed) {

    }

    protected void handleBatchEndAck(Session session, BatchEndAck batchEndAck) throws IOException {
        // send disconnect
        Disconnect disconnect = new Disconnect();
        String user = (String) session.getUserProperties().get("user");
        String collection = (String) session.getUserProperties().get("collection");

        disconnect.setHeader(createHeader(session, MessageType.Disconnect, collection, user,
            mockRepository.getServerId(), batchEndAck.getHeader().getTransactionId()));
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(disconnect));

        mockRepository.getCollectionReplicaMap().get(collection).remove(batchEndAck.getHeader().getOrigin());
        mockRepository.getUserReplicaMap().get(user).remove(batchEndAck.getHeader().getOrigin());
        mockRepository.getAuthorizedSessions().remove(session);
    }

    private ServerLastWriteWinMap createCrdt(String collection) {
        NitriteCollection nc = mockRepository.getDb().getCollection(collection);
        NitriteMap<NitriteId, Tombstone> nitriteMap =
            mockRepository.getDb().getConfig().getNitriteStore().openMap(collection + "-replica",
                NitriteId.class, Tombstone.class);
        return new ServerLastWriteWinMap(nc, nitriteMap);
    }

    private void sendErrorMessage(Session session, Throwable error) throws IOException {
        ErrorMessage errorMessage = new ErrorMessage();
        String user = (String) session.getUserProperties().get("user");
        String collection = (String) session.getUserProperties().get("collection");

        errorMessage.setHeader(createHeader(session, MessageType.Error, collection, user,
            mockRepository.getServerId(), ""));
        errorMessage.setError(error.getMessage());
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(errorMessage));
    }


    private MessageHeader createHeader(Session session, MessageType messageType, String collection,
                                       String userName, String origin, String correlationId) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setTransactionId(correlationId);
        messageHeader.setCollection(collection);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(origin);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        messageHeader.setTenant((String) session.getUserProperties().get("tenant"));
        return messageHeader;
    }

    private boolean isValidAuth(String userName, String authToken) {
        if (mockRepository.getUserMap().containsKey(userName)) {
            return mockRepository.getUserMap().get(userName).equals(authToken);
        }
        return false;
    }

    private void checkAuthorized(Session session) {
        if (!(boolean) session.getUserProperties().get("authorized")) {
            throw new SecurityException("session is not authorized");
        }
    }
}
