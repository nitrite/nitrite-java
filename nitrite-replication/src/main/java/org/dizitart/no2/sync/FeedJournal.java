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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class FeedJournal {
    private static final String JOURNAL = "journal";

    private final ReplicationTemplate replicationTemplate;
    private final ReentrantLock lock;

    public FeedJournal(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
        this.lock = new ReentrantLock();
    }

    public Receipt accumulate(Receipt receipt) {
        try {
            lock.lock();
            Receipt current = getCurrent();
            if (receipt != null && current != null) {
                for (String id : receipt.getAdded()) {
                    current.getAdded().remove(id);
                }

                for (String id : receipt.getRemoved()) {
                    current.getRemoved().remove(id);
                }
            }
            setCurrent(current);
            return current;
        } finally {
            lock.unlock();
        }
    }

    public void write(LastWriteWinState state) {
        try {
            lock.lock();
            if (state != null) {
                Receipt receipt = getCurrent();

                Set<Document> changes = state.getChanges();
                if (changes != null && !changes.isEmpty()) {
                    for (Document change : changes) {
                        receipt.getAdded().add(change.getId().getIdValue());
                    }
                }

                Map<String, Long> tombstones = state.getTombstones();
                if (tombstones != null && !tombstones.isEmpty()) {
                    for (Map.Entry<String, Long> entry : tombstones.entrySet()) {
                        receipt.getRemoved().add(entry.getKey());
                    }
                }

                setCurrent(receipt);
            }
        } finally {
            lock.unlock();
        }
    }

    public Receipt getFinalReceipt() {
        try {
            lock.lock();
            return getCurrent();
        } finally {
            lock.unlock();
        }
    }

    private Receipt getCurrent() {
        try {
            Attributes attributes = replicationTemplate.getAttributes();
            String json = attributes.get(JOURNAL);
            if (StringUtils.isNullOrEmpty(json)) {
                return new Receipt(new HashSet<>(), new HashSet<>());
            }

            ObjectMapper objectMapper = replicationTemplate.getConfig().getObjectMapper();
            return objectMapper.readValue(json, Receipt.class);
        } catch (JsonProcessingException e) {
            log.error("Error while opening replica ledger", e);
            throw new ReplicationException("failed to open replica ledger", e, false);
        }
    }

    private void setCurrent(Receipt receipt) {
        try {
            ObjectMapper objectMapper = replicationTemplate.getConfig().getObjectMapper();
            String json = objectMapper.writeValueAsString(receipt);
            Attributes attributes = replicationTemplate.getAttributes();
            attributes.set(JOURNAL, json);

            replicationTemplate.saveAttributes(attributes);
        } catch (JsonProcessingException e) {
            log.error("Error while writing replica ledger", e);
            throw new ReplicationException("failed to write replica ledger", e, false);
        }
    }
}
