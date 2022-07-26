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
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.DeltaStates;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.meta.Attributes.FEED_LEDGER;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class FeedLedger {

    private final Config config;
    private NitriteCollection journal;

    public FeedLedger(Config config) {
        this.config = config;
        initializeLedger();
    }

    public void writeOff(Receipt receipt) {
        Receipt current = getCurrent();
        if (receipt != null) {
            for (String id : receipt.getAdded()) {
                current.getAdded().remove(id);
            }

            for (String id : receipt.getRemoved()) {
                current.getRemoved().remove(id);
            }
        }
        setCurrent(current);
    }

    public void writeEntry(DeltaStates state) {
        if (state != null) {
            Receipt receipt = getCurrent();

            Set<Document> changes = state.getChangeSet();
            if (changes != null && !changes.isEmpty()) {
                for (Document change : changes) {
                    receipt.getAdded().add(change.getId().getIdValue());
                }
            }

            Map<String, Long> tombstones = state.getTombstoneMap();
            if (tombstones != null && !tombstones.isEmpty()) {
                for (Map.Entry<String, Long> entry : tombstones.entrySet()) {
                    receipt.getRemoved().add(entry.getKey());
                }
            }

            setCurrent(receipt);
        }
    }

    public Receipt getFinalReceipt() {
        return getCurrent();
    }

    private void initializeLedger() {
        NitriteCollection collection = config.getCollection();
        String feedLedgerName = getFeedLedgerName(collection);

        Nitrite db = config.getDb();
        this.journal = db.getCollection(feedLedgerName);
    }

    private String getFeedLedgerName(NitriteCollection collection) {
        Attributes attributes = collection.getAttributes();
        String feedLedgerName = attributes.get(FEED_LEDGER);
        if (StringUtils.isNullOrEmpty(feedLedgerName)) {
            feedLedgerName = collection.getName() + "_" + FEED_LEDGER;
            attributes.set(FEED_LEDGER, feedLedgerName);
            collection.setAttributes(attributes);
        }
        return feedLedgerName;
    }

    private Receipt getCurrent() {
        Document document = journal.find().firstOrNull();
        if (document == null) {
            Receipt receipt = new Receipt(new HashSet<>(), new HashSet<>());
            document = receipt.toDocument();
            journal.insert(document);
            return receipt;
        } else {
            return Receipt.fromDocument(document);
        }
    }

    private void setCurrent(Receipt receipt) {
        Document document = journal.find().firstOrNull();
        if (document == null) {
            document = receipt.toDocument();
            journal.insert(document);
        } else {
            document.put("added", receipt.getAdded());
            document.put("removed", receipt.getRemoved());
            journal.update(document);
        }
    }
}
