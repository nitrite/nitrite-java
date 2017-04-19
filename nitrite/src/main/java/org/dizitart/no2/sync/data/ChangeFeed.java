package org.dizitart.no2.sync.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.event.ChangeType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dizitart.no2.event.ChangeType.REMOVE;
import static org.dizitart.no2.event.ChangeType.UPDATE;

/**
 * Represents a cumulative changes (add, update, delete) in a collection.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@ToString
public class ChangeFeed implements Serializable {
    private static final long serialVersionUID = 1486043678L;

    /**
     * The originator of the sync request.
     *
     * @param originator the originator of the sync request
     * @return the originator of the request.
     * */
    @Getter @Setter
    private String originator;

    /**
     * The sequence number of the current feed.
     *
     * @param sequenceNumber the sequence number of the feed
     * @return the sequence number of the feed.
     * */
    @Getter @Setter
    private Long sequenceNumber;


    private Map<ChangeType, List<Document>> changeBucket;

    /**
     * Instantiates a new {@link ChangeFeed}.
     */
    public ChangeFeed() {
        changeBucket = new HashMap<>();
    }

    /**
     * Gets updated documents.
     *
     * @return the updated documents
     */
    public List<Document> getModifiedDocuments() {
        return changeBucket.get(UPDATE);
    }

    /**
     * Gets removed ids.
     *
     * @return the removed ids
     */
    public List<Document> getRemovedDocuments() {
        return changeBucket.get(REMOVE);
    }

    /**
     * Sets inserted documents.
     *
     * @param documents the documents
     */
    public void setModifiedDocuments(List<Document> documents) {
        List<Document> documentList = changeBucket.get(UPDATE);
        if (documentList == null) {
            documentList = new ArrayList<>();
        }
        if (documents != null) {
            documentList.addAll(documents);
        }
        changeBucket.put(UPDATE, documentList);
    }

    /**
     * Sets deleted documents.
     *
     *  @param documents the documents
     */
    public void setRemovedDocuments(List<Document> documents) {
        List<Document> documentList = changeBucket.get(REMOVE);
        if (documentList == null) {
            documentList = new ArrayList<>();
        }

        if (documents != null) {
            documentList.addAll(documents);
        }
        changeBucket.put(REMOVE, documentList);
    }
}
