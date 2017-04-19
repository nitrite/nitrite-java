package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the base event listener for replication event.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Getter @Setter
public abstract class SyncEventListener {
    /**
     * Gets the source collection name.
     *
     * @param collectionName source collection name
     * @return collection name.
     * */
    private String collectionName;

    /**
     * Listener routine to be invoked for each replication event.
     *
     * @param eventInfo the replication event data
     */
    public abstract void onSyncEvent(SyncEventData eventInfo);
}
