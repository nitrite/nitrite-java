package org.dizitart.no2.datagate.services;

import org.dizitart.no2.datagate.models.SyncLog;
import org.dizitart.no2.sync.data.UserAgent;
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
