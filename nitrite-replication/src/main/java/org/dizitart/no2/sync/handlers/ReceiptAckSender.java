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

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.Receipt;
import org.dizitart.no2.sync.message.ReceiptAware;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptAckSender<Ack extends DataGateMessage> {
    ReplicationTemplate getReplicationTemplate();

    Ack createAck(String correlationId, Receipt receipt);

    default void sendAck(ReceiptAware message) {
        if (message != null) {
            LastWriteWinState state = message.getFeed();
            getReplicationTemplate().getCrdt().merge(state);

            Receipt receipt = message.calculateReceipt();
            Ack ack = createAck(message.getHeader().getId(), receipt);
            MessageTemplate messageTemplate = getReplicationTemplate().getMessageTemplate();
            messageTemplate.sendMessage(ack);
        }
    }
}
