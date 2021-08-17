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

package org.dizitart.no2.sync.handlers;

import okhttp3.WebSocket;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.DataGateClient;
import org.dizitart.no2.sync.ReplicatedCollection;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.Receipt;
import org.dizitart.no2.sync.message.ReceiptAware;
import org.dizitart.no2.sync.message.TimeBoundMessage;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptAckSender<Ack extends DataGateMessage> {
    ReplicatedCollection getReplicatedCollection();

    Ack createAck(String correlationId, Receipt receipt);

    default void sendAck(WebSocket webSocket, ReceiptAware message) {
        if (message != null) {
            LastWriteWinState state = message.getFeed();
            getReplicatedCollection().getLastWriteWinMap().merge(state);

            Receipt receipt = message.calculateReceipt();
            Ack ack = createAck(message.getHeader().getTransactionId(), receipt);

            if (ack instanceof TimeBoundMessage && message instanceof TimeBoundMessage) {
                TimeBoundMessage timeBoundMessage = (TimeBoundMessage) message;
                TimeBoundMessage timeBoundAck = (TimeBoundMessage) ack;

                timeBoundAck.setStartTime(timeBoundMessage.getStartTime());
                timeBoundAck.setEndTime(timeBoundMessage.getEndTime());
            }

            DataGateClient dataGateClient = getReplicatedCollection().getDataGateClient();
            dataGateClient.sendMessage(webSocket, ack);
        }
    }
}
