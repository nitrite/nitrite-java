package org.dizitart.no2.event;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;

/**
 * Represents a collection change information.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Getter @Setter
public class ChangeInfo implements Serializable {
    /**
     * Specifies the change type.
     *
     * @param changeType the change type.
     * @returns the change type.
     * */
    private ChangeType changeType;

    /**
     * Specifies affected items due to collection
     * a change.
     *
     * @param changedItems affected items.
     * @returns set of affected items.
     * */
    private Collection<ChangedItem> changedItems;

    /**
     * Specifies the name of the thread where the change
     * has been originated.
     *
     * @param originatingThread name of originating thread.
     * @returns name of originating thread.
     * */
    private String originatingThread;

    /**
     * Instantiates a new {@link ChangeInfo}.
     *
     * @param changeType the change type
     */
    public ChangeInfo(final ChangeType changeType) {
        this.changeType = changeType;
    }
}
