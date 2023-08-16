package org.dizitart.no2.support.exchange;

import org.dizitart.no2.Nitrite;


/**
 * A factory interface to create a {@link Nitrite} instance.
 *
 * @since 4.0.0
 * @see Nitrite
 * @author Anindya Chatterjee
 * */
@FunctionalInterface
public interface NitriteFactory {
    /**
     * Creates a {@link Nitrite} instance.
     *
     * @return the nitrite instance.
     */
    Nitrite create();
}
