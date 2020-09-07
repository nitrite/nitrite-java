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

package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.tx.TransactionConfig;
import org.dizitart.no2.store.tx.TransactionalStore;

/**
 * @author Anindya Chatterjee.
 */
class DefaultNitriteCollection extends BaseNitriteCollection implements NitriteCollection {

    DefaultNitriteCollection(String name, NitriteMap<NitriteId, Document> nitriteMap, NitriteConfig nitriteConfig) {
        super(name, nitriteMap, nitriteConfig);
    }

    public TransactionalCollection beginTransaction() {
        writeLock.lock();
        TransactionalStore<?> transactionalStore = new TransactionalStore<>(nitriteStore);
        TransactionConfig transactionConfig = new TransactionConfig(nitriteConfig, transactionalStore);
        NitriteMap<NitriteId, Document> txMap = transactionalStore.openMap(collectionName, NitriteId.class, Document.class);
        return new DefaultTransactionalCollection(collectionName, txMap, transactionConfig, this, writeLock);
    }
}
