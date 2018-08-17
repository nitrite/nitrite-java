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

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.datagate.UnAuthorizedAccessException;
import org.dizitart.no2.datagate.security.RequestContext;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.sync.TimeSpan;
import org.dizitart.no2.sync.types.ChangeFeed;
import org.dizitart.no2.sync.types.FeedOptions;
import org.dizitart.no2.sync.types.InfoResponse;
import org.dizitart.no2.sync.types.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.dizitart.no2.collection.FindOptions.limit;
import static org.dizitart.no2.datagate.Constants.VENDOR;
import static org.dizitart.no2.datagate.Constants.VERSION;

/**
 * Data Gate synchronization service.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
@Service
public class DataGateService {
    @Autowired
    private ReplicaStoreService storeService;

    @Autowired
    private SyncLogService syncLogService;

    private InfoResponse infoResponse;

    public InfoResponse getServerInfo() {
        if (infoResponse == null) {
            infoResponse = new InfoResponse();
            infoResponse.setVendor(VENDOR);
            infoResponse.setVersion(VERSION);

            InfoResponse.Storage storage = new InfoResponse.Storage();
            storage.setVendor(storeService.getStoreVendor());
            storage.setVersion(storeService.getStoreVersion());
            infoResponse.setStorage(storage);

            InfoResponse.Platform platform = new InfoResponse.Platform();
            platform.setArch(System.getProperty("os.arch"));
            platform.setOs(System.getProperty("os.name"));
            platform.setJava("Java " + System.getProperty("java.version"));
            infoResponse.setPlatform(platform);
        }

        return infoResponse;
    }

    public void validateRequest(String collection) {
        UserAccount userAccount = RequestContext.getInstance().get();
        if (userAccount.getCollections() == null
                || !userAccount.getCollections().contains(collection)) {
            throw new UnAuthorizedAccessException("The user '" + userAccount.getUserName()
                    + "' is not authorized to access collection - " + collection);
        }
    }

    public ChangeFeed changedSince(String collection, FeedOptions feedOptions) {
        ChangeFeed changeFeed = new ChangeFeed();
        long newSequence = System.currentTimeMillis();
        changeFeed.setSequenceNumber(newSequence);
        changeFeed.setRemovedDocuments(
                storeService.removedItems(collection, feedOptions.getFromSequence(), newSequence));
        changeFeed.setModifiedDocuments(
                storeService.modifiedItems(collection, feedOptions.getFromSequence(), newSequence));

        return changeFeed;
    }

    public boolean change(String collection, ChangeFeed changeFeed) {
        if (changeFeed.getRemovedDocuments() != null) {
            storeService.remove(collection, changeFeed.getRemovedDocuments());
        }

        if (changeFeed.getModifiedDocuments() != null) {
            storeService.modify(collection, changeFeed.getModifiedDocuments());
        }
        return true;
    }

    public List<Document> fetch(String collection, int offset, int size) {
        return storeService.findAll(collection, limit(offset, size));
    }

    public long size(String collection) {
        return storeService.size(collection);
    }

    public void clear(String collection) {
        storeService.clear(collection);
    }

    public boolean tryLock(String collection, String issuer,
                           String userAgentString, TimeSpan expiryDelay) {
        Attributes attributes = storeService.getAttributes(collection);
        if (attributes == null) {
            attributes = new Attributes(collection);
        }
        long syncLock = attributes.getSyncLock();
        long expiryWait = attributes.getExpiryWait();
        long expiryTime = syncLock + expiryWait;   // in milliseconds
        if (syncLock == 0
                || expiryTime < System.currentTimeMillis()) {
            // acquire lock
            acquireLock(collection, issuer, userAgentString, attributes, expiryDelay);
            log.debug("Remote lock acquired");
            return true;
        }
        log.debug("Failed to acquire remote lock");
        return false;
    }

    public void releaseLock(String collection, String issuer) {
        Attributes attributes = storeService.getAttributes(collection);
        attributes.setSyncLock(0);
        attributes.setExpiryWait(0);
        storeService.setAttributes(collection, attributes);
        log.debug("Remote lock released");
        syncLogService.releaseLock(issuer, collection, System.currentTimeMillis());
    }

    private void acquireLock(String collection, String issuer,
                             String userAgentString,
                             Attributes attributes, TimeSpan expireDelay) {
        attributes.setSyncLock(System.currentTimeMillis());
        attributes.setExpiryWait(MILLISECONDS
                .convert(expireDelay.getTime(), expireDelay.getTimeUnit()));
        storeService.setAttributes(collection, attributes);
        syncLogService.acquireLock(issuer, userAgentString, collection, attributes.getSyncLock());
    }
}
