package org.dizitart.no2.migration;

/**
 * @author Anindya Chatterjee
 */
public enum InstructionType {
    // db related statements
    AddPassword,
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
    RenameRepository,
    RepositoryAddField,
    RepositoryRenameField,
    RepositoryDeleteField,
    RepositoryChangeDataType,
    RepositoryChangeIdField,
    RepositoryDropIndex,
    RepositoryDropIndices,
    RepositoryCreateIndex
}
