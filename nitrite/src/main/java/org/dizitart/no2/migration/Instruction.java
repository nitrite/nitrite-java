package org.dizitart.no2.migration;

import static org.dizitart.no2.common.util.ObjectUtils.getEntityName;

/**
 * @author Anindya Chatterjee
 */
public interface Instruction {
    DatabaseInstruction forDatabase();

    default RepositoryInstruction forRepository(String typeName) {
        return forRepository(typeName, null);
    }

    default RepositoryInstruction forRepository(Class<?> type) {
        return forRepository(getEntityName(type), null);
    }

    default RepositoryInstruction forRepository(Class<?> type, String key) {
        return forRepository(getEntityName(type), key);
    }

    RepositoryInstruction forRepository(String typeName, String key);

    CollectionInstruction forCollection(String collectionName);
}
