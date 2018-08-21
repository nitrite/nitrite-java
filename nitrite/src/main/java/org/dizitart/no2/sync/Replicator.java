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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.common.ExecutorServiceManager;
import org.dizitart.no2.event.EventBus;

import java.util.concurrent.ScheduledExecutorService;

import static org.dizitart.no2.collection.IndexOptions.indexOptions;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.exceptions.ErrorCodes.VE_SYNC_NULL_COLLECTION;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * Replication configurator.
 *
 * [[app-listing]]
 * .Example
 * include::/src/docs/asciidoc/replication/example.adoc[]
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class Replicator {

    /**
     * Sets the local database to start replication.
     *
     * @param db the local Nitrite db.
     * @return the {@link CollectionSyncBuilder} instance.
     */
    public static CollectionSyncBuilder of(Nitrite db) {
        CollectionSyncBuilder builder = new CollectionSyncBuilder();
        builder.db = db;
        return builder;
    }

    /**
     * Local collection builder for replication.
     */
    public static class CollectionSyncBuilder {
        private Nitrite db;

        /**
         * Sets the local {@link NitriteCollection}.
         *
         * @param collection the collection
         * @return the {@link SyncHandleBuilder} instance.
         */
        public SyncHandleBuilder forLocal(NitriteCollection collection) {
            SyncHandleBuilder syncHandleBuilder = new SyncHandleBuilder();
            syncHandleBuilder.db = this.db;
            syncHandleBuilder.collection = collection;
            return syncHandleBuilder;
        }

        /**
         * Sets the local {@link ObjectRepository}.
         *
         * @param <T>        the type parameter
         * @param repository the object repository
         * @return the {@link SyncHandleBuilder} instance.
         */
        public <T> SyncHandleBuilder forLocal(ObjectRepository<T> repository) {
            SyncHandleBuilder syncHandleBuilder = new SyncHandleBuilder();
            syncHandleBuilder.db = this.db;
            syncHandleBuilder.collection = repository.getDocumentCollection();
            return syncHandleBuilder;
        }
    }

    /**
     * The {@link SyncHandle} builder for replication.
     */
    public static class SyncHandleBuilder {
        private Nitrite db;
        private NitriteCollection collection;
        private SyncTemplate syncTemplate;
        private ReplicationType replicationType;
        private TimeSpan syncDelay;
        private SyncEventListener listener;

        /**
         * Sets the {@link SyncTemplate} for replication.
         *
         * @param syncTemplate the sync template implementation
         * @return the {@link SyncHandleBuilder} instance.
         */
        public SyncHandleBuilder withSyncTemplate(SyncTemplate syncTemplate) {
            this.syncTemplate = syncTemplate;
            return this;
        }

        /**
         * Sets the {@link ReplicationType}.
         *
         * @param replicationType the replication type
         * @return the {@link SyncHandleBuilder} instance.
         */
        public SyncHandleBuilder ofType(ReplicationType replicationType) {
            this.replicationType = replicationType;
            return this;
        }

        /**
         * Sets the sync delay. It is the time gap between 2 successive
         * replication.
         *
         * @param syncDelay the sync delay
         * @return the {@link SyncHandleBuilder} instance.
         */
        public SyncHandleBuilder delay(TimeSpan syncDelay) {
            this.syncDelay = syncDelay;
            return this;
        }

        /**
         * Sets the {@link SyncEventListener} to listen to sync events.
         *
         * @param listener the listener
         * @return the {@link SyncHandleBuilder} instance.
         */
        public SyncHandleBuilder withListener(SyncEventListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Configures a new {@link SyncHandle} to control the replication.
         *
         * @return the sync handle
         */
        @SuppressWarnings("unchecked")
        public SyncHandle configure() {
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setSyncTemplate(syncTemplate);
            syncConfig.setReplicationType(replicationType);
            syncConfig.setSyncDelay(syncDelay);
            syncConfig.setSyncEventListener(listener);

            EventBus<SyncEventData, SyncEventListener> eventBus
                    = new SyncEventBus();
            NitriteCollection changeLogRepository
                    = db.getCollection("removeLog");
            ScheduledExecutorService replicatorPool
                    = ExecutorServiceManager.scheduledExecutor();

            notNull(collection, errorMessage("collection can not be null", VE_SYNC_NULL_COLLECTION));
            String uniqueName = collection.getName();
            RemoveLogWriter removeLogWriter
                    = new RemoveLogWriter(changeLogRepository);
            removeLogWriter.setCollection(uniqueName);
            collection.register(removeLogWriter);

            SyncService syncService = new SyncService();
            if (collection instanceof ObjectRepository) {
                collection = ((ObjectRepository) collection).getDocumentCollection();
            }
            syncService.setSyncConfig(syncConfig);
            syncService.setChangeLogRepository(changeLogRepository);
            syncService.setLocalCollection(new LocalCollection(collection));
            syncService.setSyncEventBus(eventBus);

            CollectionReplicator replicator = new CollectionReplicator();
            replicator.setSyncService(syncService);
            replicator.setReplicationType(syncConfig.getReplicationType());

            SyncHandle syncHandle = new SyncHandle(syncConfig);
            syncHandle.setReplicatorPool(replicatorPool);
            syncHandle.setSyncService(syncService);
            syncHandle.setReplicator(replicator);

            SyncEventListener listener = syncConfig.getSyncEventListener();
            if (listener != null) {
                listener.setCollectionName(uniqueName);
                eventBus.register(listener);
            }

            if (!collection.hasIndex(DOC_REVISION) && !collection.isIndexing(DOC_REVISION)) {
                collection.createIndex(DOC_REVISION, indexOptions(IndexType.NonUnique, true));
            }

            return syncHandle;
        }
    }
}
