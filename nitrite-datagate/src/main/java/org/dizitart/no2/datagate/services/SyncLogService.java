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

package org.dizitart.no2.datagate.services;

import org.dizitart.no2.datagate.models.SyncLog;
import org.dizitart.no2.sync.types.UserAgent;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static org.dizitart.no2.datagate.Constants.SYNC_LOG;

/**
 * An asynchronous service to log all sync operation
 * on the server.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Service
public class SyncLogService {

    @Autowired
    private Jongo jongo;

    @Async
    public void acquireLock(String issuer, String userAgentString,
                            String collection, long lockTime) {
        MongoCollection syncLogCollection = jongo.getCollection(SYNC_LOG);
        SyncLog syncLog = syncLogCollection.findOne("{issuer: #}", issuer).as(SyncLog.class);

        boolean firstLog = false;
        if (syncLog == null) {
            syncLog = new SyncLog();
            firstLog = true;
        }

        syncLog.setIssuer(issuer);
        syncLog.setUserAgent(UserAgent.parse(userAgentString));
        syncLog.setCollection(collection);
        syncLog.setLockAcquired(lockTime);
        syncLog.setLockReleased(0);

        if (firstLog) {
            syncLogCollection.save(syncLog);
        } else {
            syncLogCollection.update("{issuer: #}", issuer).with(syncLog);
        }
    }

    @Async
    public void releaseLock(String issuer, String collection, long releaseTime) {
        MongoCollection syncLogCollection = jongo.getCollection(SYNC_LOG);
        SyncLog syncLog = syncLogCollection.findOne("{issuer: #}", issuer).as(SyncLog.class);

        if (syncLog == null) {
            return;
        }

        syncLog.setIssuer(issuer);
        syncLog.setCollection(collection);
        syncLog.setLockReleased(releaseTime);
        syncLogCollection.update("{issuer: #}", issuer).with(syncLog);
    }
}
