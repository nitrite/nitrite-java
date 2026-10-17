package org.dizitart.nitrite.test.serialization.model;

import java.io.Serializable;

/**
 * A {@link Serializable} type outside of the default deserialization allowlist.
 */
public class UntrustedPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    public UntrustedPayload(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
