package org.dizitart.no2.sync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * The DataGate server tryLock operation response.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class TryLockResponse implements Serializable {
    private static final long serialVersionUID = 1487242693L;

    /**
     * The boolean value indicating whether the lock
     * has been acquired or not.
     *
     * @param lockAcquired the boolean value
     * @return `true` if lock acquired; `false` otherwise.
     * */
    private boolean lockAcquired;

    /**
     * Instantiates a new {@link TryLockResponse}.
     */
    public TryLockResponse() {}
}
