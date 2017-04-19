package org.dizitart.no2.sync;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.event.EventBus;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.concurrent.ScheduledExecutorService;

import static org.dizitart.no2.Constants.DOC_REVISION;
import static org.dizitart.no2.IndexOptions.indexOptions;
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
                    = new SyncEventBus(db.getContext());
            NitriteCollection changeLogRepository
                    = db.getCollection("removeLog");
            ScheduledExecutorService replicatorPool
                    = db.getContext().getScheduledWorkerPool();

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
