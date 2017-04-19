package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Represents the event data for a replication event.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Getter @Setter
public class SyncEventData implements Serializable {
    /**
     * Indicates the source collection name.
     *
     * @param collectionName source collection name
     * @return collection name.
     * */
    private String collectionName;

    /**
     * Indicates the replication {@link EventType}.
     *
     * @param eventType replication event type
     * @return replication event type.
     * */
    private EventType eventType;

    /**
     * Indicates error during replication.
     *
     * @param error replication error
     * @return error in replication.
     * */
    private Throwable error;
}
