package org.dizitart.no2;

/**
 * An enum to specify {@link Index} type.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public enum IndexType {
    /**
     * Specifies an unique index.
     */
    Unique,
    /**
     * Specifies a non unique index.
     */
    NonUnique,
    /**
     * Specifies a fulltext search index.
     */
    Fulltext
}
