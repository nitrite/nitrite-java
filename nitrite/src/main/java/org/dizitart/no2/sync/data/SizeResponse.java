package org.dizitart.no2.sync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * The DataGate server size operation response.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class SizeResponse implements Serializable {
    private static final long serialVersionUID = 1487242966L;

    /**
     * The size of the server collection.
     *
     * @param size the size of the server collection
     * @return the size of the sever collection.
     * */
    private long size;

    /**
     * Instantiates a new {@link SizeResponse}.
     */
    public SizeResponse(){}
}
