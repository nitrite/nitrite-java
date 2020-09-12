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
        return new RepositoryInstruction() {
            @Override
            public String entityName() {
                return typeName;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public void addStep(MigrationStep step) {
                migrationSteps.add(step);
            }
        };
    }

    @Override
    public CollectionInstruction forCollection(String collectionName) {
        return new CollectionInstruction() {
            @Override
            public String collectionName() {
                return collectionName;
            }

            @Override
            public void addStep(MigrationStep step) {
                migrationSteps.add(step);
            }
        };
    }
}
