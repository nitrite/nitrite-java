package org.dizitart.no2.migration;

/**
 * @author Anindya Chatterjee
 */
public interface Instruction {
    DatabaseInstruction forDatabase();

    default RepositoryInstruction forRepository(String typeName) {
        return forRepository(typeName, null);
    }

    default RepositoryInstruction forRepository(Class<?> type) {
        return forRepository(type.getName(), null);
    }

    default RepositoryInstruction forRepository(Class<?> type, String key) {
        return forRepository(type.getName(), key);
    }

    RepositoryInstruction forRepository(String typeName, String key);

    CollectionInstruction forCollection(String collectionName);
}
