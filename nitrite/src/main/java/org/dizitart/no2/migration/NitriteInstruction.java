package org.dizitart.no2.migration;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Queue;

/**
 * @author Anindya Chatterjee
 */
class NitriteInstruction implements Instruction {
    @Getter(AccessLevel.PACKAGE)
    private final Queue<MigrationStep> migrationSteps;

    NitriteInstruction(Queue<MigrationStep> migrationSteps) {
        this.migrationSteps = migrationSteps;
    }

    @Override
    public DatabaseInstruction forDatabase() {
        return migrationSteps::add;
    }

    @Override
    public RepositoryInstruction forRepository(String typeName, String key) {
        return migrationSteps::add;
    }

    @Override
    public CollectionInstruction forCollection(String collectionName) {
        return migrationSteps::add;
    }
}
