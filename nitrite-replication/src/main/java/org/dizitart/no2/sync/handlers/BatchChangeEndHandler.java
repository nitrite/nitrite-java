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

import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchChangeEnd;
import org.dizitart.no2.sync.message.BatchEndAck;

/**
 * @author Anindya Chatterjee
 */
public class BatchChangeEndHandler implements MessageHandler<BatchChangeEnd> {
    private ReplicationTemplate replicationTemplate;

    public BatchChangeEndHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(BatchChangeEnd message) {
        MessageFactory factory = replicationTemplate.getMessageFactory();
        BatchEndAck batchEndAck = factory.createBatchEndAck(replicationTemplate.getConfig(),
            replicationTemplate.getReplicaId(), message.getHeader().getId());

        MessageTemplate messageTemplate = replicationTemplate.getMessageTemplate();
        messageTemplate.sendMessage(batchEndAck);
        Long time = message.getHeader().getTimestamp();
        replicationTemplate.saveLastSyncTime(time);
        replicationTemplate.setAcceptCheckpoint();
    }
}
