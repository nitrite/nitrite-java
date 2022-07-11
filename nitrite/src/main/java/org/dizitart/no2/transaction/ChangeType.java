package org.dizitart.no2.transaction;

/**
 * Represents a change type in a transaction.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
enum ChangeType {
    /**
     * Insert
     */
    Insert,

    /**
     * Update.
     */
    Update,

    /**
     * Remove.
     */
    Remove,

    /**
     * Clear.
     */
    Clear,

    /**
     * Create index.
     */
    CreateIndex,

    /**
     * Rebuild index.
     */
    RebuildIndex,

    /**
     * Drop index.
     */
    DropIndex,

    /**
     * Drop all indices.
     */
    DropAllIndexes,

    /**
     * Drop collection.
     */
    DropCollection,

    /**
     * Set attribute.
     */
    SetAttributes,
}
