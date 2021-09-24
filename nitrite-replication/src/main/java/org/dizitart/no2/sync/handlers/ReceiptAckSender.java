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
import org.dizitart.no2.sync.crdt.DeltaStates;
import org.dizitart.no2.sync.DataGateSocketListener;
import org.dizitart.no2.sync.ReplicatedCollection;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptAckSender<Ack extends DataGateMessage> {
    ReplicatedCollection getReplicatedCollection();

    Ack createAck(String transactionId, Receipt receipt);

    default void sendAck(WebSocket webSocket, ReceiptAware message) {
        if (message != null) {
            DeltaStates state = message.getFeed();
            getReplicatedCollection().getReplicatedDataType().merge(state);

            Receipt receipt = message.calculateReceipt();
            Ack ack = createAck(message.getHeader().getTransactionId(), receipt);
            ack.getHeader().setCorrelationId(message.getHeader().getId());

            BatchMessage batchMessage = (BatchMessage) message;
            BatchMessage batchAck = (BatchMessage) ack;

            // set offset and batch size
            batchAck.setNextOffset(batchMessage.getNextOffset());
            batchAck.setBatchSize(batchMessage.getBatchSize());

            // set start time and end time
            batchAck.setStartTime(batchMessage.getStartTime());
            batchAck.setEndTime(batchMessage.getEndTime());

            DataGateSocketListener dataGateSocketListener
                = getReplicatedCollection().getDataGateSocketListener();
            dataGateSocketListener.sendMessage(webSocket, ack);
        }
    }
}
