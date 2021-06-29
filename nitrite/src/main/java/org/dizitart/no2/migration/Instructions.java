package org.dizitart.no2.migration;

import static org.dizitart.no2.common.util.ObjectUtils.getEntityName;

/**
 * Represents a set of instruction to perform during database migration.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Instructions {
    /**
     * Creates a {@link DatabaseInstruction}.
     *
     * @return the database instruction
     */
    DatabaseInstruction forDatabase();

    /**
     * Creates a {@link RepositoryInstruction}.
     *
     * @param typeName the type name
     * @return the repository instruction
     */
    default RepositoryInstruction forRepository(String typeName) {
        return forRepository(typeName, null);
    }

    /**
     * Creates a {@link RepositoryInstruction}.
     *
     * @param type the type
     * @return the repository instruction
     */
    default RepositoryInstruction forRepository(Class<?> type) {
        return forRepository(getEntityName(type), null);
    }

    /**
     * Creates a {@link RepositoryInstruction}.
     *
     * @param type the type
     * @param key  the key
     * @return the repository instruction
     */
    default RepositoryInstruction forRepository(Class<?> type, String key) {
        return forRepository(getEntityName(type), key);
    }

    /**
     * Creates a {@link RepositoryInstruction}.
     *
     * @param typeName the type name
     * @param key      the key
     * @return the repository instruction
     */
    RepositoryInstruction forRepository(String typeName, String key);

    /**
     * Creates a {@link CollectionInstruction}.
     *
     * @param collectionName the collection name
     * @return the collection instruction
     */
    CollectionInstruction forCollection(String collectionName);
}
