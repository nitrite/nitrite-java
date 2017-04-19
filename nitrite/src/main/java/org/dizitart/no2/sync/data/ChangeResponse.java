package org.dizitart.no2.sync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * The DataGate server change operation response.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@AllArgsConstructor
public class ChangeResponse implements Serializable {
    private static final long serialVersionUID = 1487242652L;

    /**
     * The boolean value indicating whether the server
     * data updated successfully or not.
     *
     * @param changed the boolean value
     * @return `true` if change is successful; `false` otherwise.
     * */
    private boolean changed;

    /**
     * Instantiates a new {@link ChangeResponse}.
     */
    public ChangeResponse(){}
}
