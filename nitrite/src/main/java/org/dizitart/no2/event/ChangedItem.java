package org.dizitart.no2.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Document;

import java.io.Serializable;

/**
 * Represents affected item during collection modification.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@ToString
@Getter @Setter
public class ChangedItem implements Serializable {
    /**
     * Specifies the changed document.
     *
     * @param document the document.
     * @returns the document.
     * */
    private Document document;

    /**
     * Specifies the change type.
     *
     * @param changeType the type of the change.
     * @returns the type of the change.
     * */
    private ChangeType changeType;

    /**
     * Specifies the unix timestamp of the change.
     *
     * @param changeTimestamp the unix timestamp of the change.
     * @returns the unix timestamp of the change.
     * */
    private long changeTimestamp;
}
