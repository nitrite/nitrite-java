package org.dizitart.no2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a key and a value pair.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class KeyValuePair {

    /**
     * The key of the pair.
     *
     * @param key the key to set.
     * @returns the key.
     * */
    private String key;

    /**
     * The value of the pair.
     *
     * @param value the value to set.
     * @returns the value.
     * */
    private Object value;
}
