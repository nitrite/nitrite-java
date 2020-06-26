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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.sync.FeedJournal;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateFeed;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Anindya Chatterjee
 */
public interface JournalAware {
    ReplicationTemplate getReplicationTemplate();

    default FeedJournal getJournal() {
        return getReplicationTemplate().getFeedJournal();
    }

    default void retryFailed(Receipt receipt) {
        if (shouldRetry(receipt)) {
            LastWriteWinState state = createState(receipt);

            MessageFactory factory = getReplicationTemplate().getMessageFactory();
            DataGateFeed feedMessage = factory.createFeedMessage(getReplicationTemplate().getConfig(),
                getReplicationTemplate().getReplicaId(), state);

            MessageTemplate messageTemplate = getReplicationTemplate().getMessageTemplate();
            messageTemplate.sendMessage(feedMessage);
        }
    }

    default LastWriteWinState createState(Receipt receipt) {
        LastWriteWinState state = new LastWriteWinState();
        state.setTombstones(new HashMap<>());
        state.setChanges(new HashSet<>());

        NitriteCollection collection = getReplicationTemplate().getCollection();
        LastWriteWinMap crdt = getReplicationTemplate().getCrdt();

        if (receipt != null) {
            if (receipt.getAdded() != null) {
                for (String id : receipt.getAdded()) {
                    Document document = collection.getById(NitriteId.createId(id));
                    if (document != null) {
                        state.getChanges().add(document);
                    }
                }
            }

            if (receipt.getRemoved() != null) {
                for (String id : receipt.getRemoved()) {
                    Long timestamp = crdt.getTombstones().get(NitriteId.createId(id));
                    if (timestamp != null) {
                        state.getTombstones().put(id, timestamp);
                    }
                }
            }
        }

        return state;
    }

    default boolean shouldRetry(Receipt receipt) {
        if (receipt == null) return false;
        if (receipt.getAdded() == null && receipt.getRemoved() == null) return false;
        return !receipt.getAdded().isEmpty() || !receipt.getRemoved().isEmpty();
    }
}
