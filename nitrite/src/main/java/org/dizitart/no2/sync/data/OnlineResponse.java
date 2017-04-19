package org.dizitart.no2.sync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * The DataGate server online operation response.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class OnlineResponse implements Serializable {
    private static final long serialVersionUID = 1487243044L;

    /**
     * The boolean value indicating whether the server
     * is online or not.
     *
     * @param online the boolean value
     * @return `true` if online; `false` otherwise.
     * */
    private boolean online;

    /**
     * Instantiates a new {@link OnlineResponse}.
     */
    public OnlineResponse(){}
}
