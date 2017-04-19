package org.dizitart.no2.sync.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dizitart.no2.Document;

import java.io.Serializable;
import java.util.List;

/**
 * The DataGate server fetch operation response.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@AllArgsConstructor
public class FetchResponse implements Serializable {
    private static final long serialVersionUID = 1487242605L;

    /**
     * The list of {@link Document}s fetched from the server.
     *
     * @param documents the list of {@link Document}s
     * @return the list of {@link Document}s fetched from server.
     * */
    private List<Document> documents;

    /**
     * Instantiates a new {@link FetchResponse}.
     */
    public FetchResponse() {}
}
