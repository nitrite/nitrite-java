package org.dizitart.no2.migration;

/**
 * @author Anindya Chatterjee
 */
public enum InstructionType {
    // db related statements
    ChangePassword,
    DropCollection,
    DropRepository,
    Custom,

    // collection related statements
    CollectionRename,
    CollectionAddField,
    CollectionRenameField,
    CollectionDeleteField,
    CollectionDropIndex,
    CollectionDropIndices,
    CollectionCreateIndex,

    // repository related statements
    RenameEntity,
    RenameKey,
    RepositoryAddField,
    RepositoryRenameField,
    RepositoryDeleteField,
    RepositoryChangeDataType,
    RepositoryChangeIdField,
    RepositoryDropIndex,
    RepositoryDropIndices,
    RepositoryCreateIndex
}
