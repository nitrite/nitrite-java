/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.ChangeType;
import org.dizitart.no2.event.ChangedItem;

import java.util.List;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.util.Iterables.toList;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
class RemoveLogWriter implements ChangeListener {
    private final NitriteCollection changeLogRepository;

    @Getter @Setter
    private String collection;

    RemoveLogWriter(NitriteCollection changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    @Override
    public void onChange(ChangeInfo changeInfo) {
        String threadName = changeInfo.getOriginatingThread();

        if (!isNullOrEmpty(threadName)
                && !threadName.contains(CollectionReplicator.class.getSimpleName())) {

            ChangeType action = changeInfo.getChangeType();
            Iterable<ChangedItem> changedItems = changeInfo.getChangedItems();

            if (action == ChangeType.REMOVE) {
                queueRemovedItems(toList(changedItems));
            }
        }
    }

    private void queueRemovedItems(List<ChangedItem> changedItems) {
        for (final ChangedItem item : changedItems) {
            Document logEntry = createDocument(COLLECTION, this.collection)
                    .put(DELETE_TIME, item.getChangeTimestamp())
                    .put(DELETED_ITEM, item.getDocument());
            changeLogRepository.insert(logEntry);
        }
    }
}
