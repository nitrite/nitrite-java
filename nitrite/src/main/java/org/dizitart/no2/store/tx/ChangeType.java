package org.dizitart.no2.store.tx;

/**
 * @author Anindya Chatterjee
 */
public enum ChangeType {
    Insert,
    Update,
    Remove,
    Clear,
    CreateIndex,
    RebuildIndex,
    DropIndex,
    DropCollection
}
