/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sync.crdt;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import static org.dizitart.no2.collection.meta.Attributes.LAST_SYNCED;
import static org.dizitart.no2.collection.meta.Attributes.TOMBSTONE;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;

/**
 * @author Anindya Chatterjee
 */
public interface ConflictFreeReplicatedDataType {
    NitriteCollection getCollection();

    String getReplicaId();

    default Attributes getAttributes() {
        Attributes attributes = getCollection().getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
            saveAttributes(attributes);
        }
        return attributes;
    }

    default Long getLastSyncTime() {
        Attributes attributes = getAttributes();
        String syncTimeStr = attributes.get(LAST_SYNCED);
        if (StringUtils.isNullOrEmpty(syncTimeStr)) {
            return 0L;
        } else {
            return Long.parseLong(syncTimeStr);
        }
    }

    default LastWriteWinMap createConflictFreeReplicatedDataType() {
        Attributes attributes = getAttributes();
        String tombstoneName = getTombstoneName(attributes);
        saveAttributes(attributes);

        NitriteStore<?> store = getCollection().getStore();
        NitriteMap<NitriteId, Long> tombstone = store.openMap(tombstoneName, NitriteId.class, Long.class);
        return new LastWriteWinMap(getCollection(), tombstone);
    }

    default String getTombstoneName(Attributes attributes) {
        String tombstoneName = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(tombstoneName)) {
            tombstoneName = getCollection().getName()
                + INTERNAL_NAME_SEPARATOR + TOMBSTONE;
            attributes.set(TOMBSTONE, tombstoneName);
        }
        return tombstoneName;
    }

    default void saveLastSyncTime(Long lastSyncTime) {
        Attributes attributes = getAttributes();
        attributes.set(LAST_SYNCED, Long.toString(lastSyncTime));
        saveAttributes(attributes);
    }

    default void saveAttributes(Attributes attributes) {
        getCollection().setAttributes(attributes);
    }
}
