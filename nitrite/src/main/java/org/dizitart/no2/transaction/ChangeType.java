package org.dizitart.no2.transaction;

/**
 * @author Anindya Chatterjee
 */
enum ChangeType {
    Insert,
    Update,
    Remove,
    Clear,
    CreateIndex,
    RebuildIndex,
    DropIndex,
    DropAllIndices,
    DropCollection,
    SetAttribute
}
