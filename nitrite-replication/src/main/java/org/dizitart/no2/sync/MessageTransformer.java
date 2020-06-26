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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public class MessageTransformer {
    private ObjectMapper objectMapper;

    public MessageTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DataGateMessage transform(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            if (isError(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, ErrorMessage.class);
            } else if (isConnect(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, Connect.class);
            } else if (isConnectAck(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, ConnectAck.class);
            } else if (isDisconnect(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, Disconnect.class);
            } else if (isBatchChangeStart(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, BatchChangeStart.class);
            } else if (isBatchChangeContinue(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, BatchChangeContinue.class);
            } else if (isBatchChangeEnd(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, BatchChangeEnd.class);
            } else if (isBatchAck(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, BatchAck.class);
            } else if (isBatchEndAck(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, BatchEndAck.class);
            } else if (isFeed(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, DataGateFeed.class);
            } else if (isFeedAck(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, DataGateFeedAck.class);
            }
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to transform message from server", e, true);
        }
        return null;
    }

    private boolean isError(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.Error.code());
    }

    private boolean isConnect(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.Connect.code());
    }

    private boolean isConnectAck(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.ConnectAck.code());
    }

    private boolean isDisconnect(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.Disconnect.code());
    }

    private boolean isBatchChangeStart(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.BatchChangeStart.code());
    }

    private boolean isBatchChangeContinue(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.BatchChangeContinue.code());
    }

    private boolean isBatchChangeEnd(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.BatchChangeEnd.code());
    }

    private boolean isBatchAck(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.BatchAck.code());
    }

    private boolean isBatchEndAck(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.BatchEndAck.code());
    }

    private boolean isFeed(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.DataGateFeed.code());
    }

    private boolean isFeedAck(JsonNode jsonNode) {
        return jsonNode.get("header").get("messageType").asText().equals(MessageType.DataGateFeedAck.code());
    }
}
