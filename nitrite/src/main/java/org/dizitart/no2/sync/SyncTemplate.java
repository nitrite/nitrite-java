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

import org.dizitart.no2.Document;
import org.dizitart.no2.sync.types.ChangeFeed;
import org.dizitart.no2.sync.types.FeedOptions;

import java.util.List;

/**
 * An interface for the DataGate sync operations.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface SyncTemplate {
    /**
     * Downloads the list of changes from the server.
     *
     * @param feedOptions options to fetch the change feed
     * @return the list of changes
     */
    ChangeFeed changedSince(FeedOptions feedOptions);

    /**
     * Uploads the local changes to the server.
     *
     * @param changeFeed the list of changes to upload
     * @return `true` if the operation is successful; `false` otherwise.
     */
    boolean change(ChangeFeed changeFeed);

    /**
     * Fetches all documents from remote.
     *
     * @param offset pagination offset
     * @param limit pagination limit
     * @return list of documents from remote.
     */
    List<Document> fetch(int offset, int limit);

    /**
     * Gets the size of the remote collection.
     *
     * @return the size in long
     */
    long size();

    /**
     * Clears the remote collection.
     */
    void clear();

    /**
     * Checks if the server is online and reachable.
     *
     * @return `true` if online; `false` otherwise.
     */
    boolean isOnline();

    /**
     * Gets the name of the remote collection.
     *
     * @return the name of the remote collection.
     */
    String getCollectionName();

    /**
     * Tries to acquire a synchronization lock on the remote collection.
     * Before start of replication, a sync lock must be acquired on remote
     * collection. If the acquisition is unsuccessful, replication will
     * not occur and it will be retried in next iteration.
     *
     * If the expiryDelay is expired, then a new lock will be acquired overwriting
     * previous lock information.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: Lock information should be maintain as a metadata in the remote database
     * in a separate collection/table than the remote collection.
     *
     * @param expiryDelay the expiry delay
     * @param issuer originator of the change feed
     * @return the boolean
     */
    boolean trySyncLock(TimeSpan expiryDelay, String issuer);

    /**
     * Releases the synchronization lock on the remote collection.
     */
    void releaseLock(String issuer);
}
