package org.dizitart.no2.migration;

/**
 * Represents an instruction type.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public enum InstructionType {
    // db related statements
    /**
     * The add user instruction.
     */
    AddUser,
    /**
     * The change password instruction.
     */
    ChangePassword,
    /**
     * The drop collection instruction.
     */
    DropCollection,
    /**
     * The drop repository instruction.
     */
    DropRepository,
    /**
     * The custom instruction.
     */
    Custom,


    // collection related statements
    /**
     * The collection rename instruction.
     */
    CollectionRename,
    /**
     * The collection add field instruction.
     */
    CollectionAddField,
    /**
     * The collection rename field instruction.
     */
    CollectionRenameField,
    /**
     * The collection delete field instruction.
     */
    CollectionDeleteField,
    /**
     * The collection drop index instruction.
     */
    CollectionDropIndex,
    /**
     * The collection drop all indices instruction.
     */
    CollectionDropIndices,
    /**
     * The collection create index instruction.
     */
    CollectionCreateIndex,


    // repository related statements
    /**
     * The rename repository instruction.
     */
    RenameRepository,
    /**
     * The repository add field instruction.
     */
    RepositoryAddField,
    /**
     * The repository rename field instruction.
     */
    RepositoryRenameField,
    /**
     * The repository delete field instruction.
     */
    RepositoryDeleteField,
    /**
     * The repository change data type instruction.
     */
    RepositoryChangeDataType,
    /**
     * The repository change id field instruction.
     */
    RepositoryChangeIdField,
    /**
     * The repository drop index instruction.
     */
    RepositoryDropIndex,
    /**
     * The repository drop indices instruction.
     */
    RepositoryDropIndices,
    /**
     * The repository create index instruction.
     */
    RepositoryCreateIndex
}
