package org.dizitart.no2.support.exchange;

import org.dizitart.no2.Nitrite;

/**
 * A functional interface for creating a {@link Nitrite} instance.
 * 
 * @since 4.0
 * @see Nitrite
 * @author Anindya Chatterjee
 */
@FunctionalInterface
public interface NitriteFactory {
    /**
     * Creates a new instance of Nitrite database.
     *
     * @return a new instance of Nitrite database.
     */
    Nitrite create();
}
