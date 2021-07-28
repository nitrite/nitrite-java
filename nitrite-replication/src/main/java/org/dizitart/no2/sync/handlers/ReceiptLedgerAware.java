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
import org.dizitart.no2.sync.Config;
import org.dizitart.no2.sync.DataGateClient;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptLedgerAware {
    NitriteCollection getCollection();

    String getReplicaId();

    LastWriteWinMap getLastWriteWinMap();

    Config getConfig();

    DataGateClient getDataGateClient();

    default LastWriteWinState createState(Receipt receipt) {
        LastWriteWinState state = new LastWriteWinState();
        state.setTombstoneMap(new HashMap<>());
        state.setChangeSet(new HashSet<>());

        NitriteCollection collection = getCollection();

        if (receipt != null) {
            if (receipt.getAdded() != null) {
                for (String id : receipt.getAdded()) {
                    Document document = collection.getById(NitriteId.createId(id));
                    if (document != null) {
                        state.getChangeSet().add(document);
                    }
                }
            }

            if (receipt.getRemoved() != null) {
                for (String id : receipt.getRemoved()) {
                    Long timestamp = getLastWriteWinMap().getTombstoneMap().get(NitriteId.createId(id));
                    if (timestamp != null) {
                        state.getTombstoneMap().put(id, timestamp);
                    }
                }
            }
        }

        return state;
    }

    default boolean shouldRetry(Receipt receipt) {
        if (receipt == null) return false;
        if (receipt.getAdded() == null) return false;
        if (receipt.getAdded() == null && receipt.getRemoved() == null) return false;
        return !receipt.getAdded().isEmpty() || !receipt.getRemoved().isEmpty();
    }
}
