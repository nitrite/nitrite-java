package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Quartet;
import org.dizitart.no2.common.tuples.Triplet;

/**
 * @author Anindya Chatterjee
 */
public interface RepositoryInstruction extends Composable {

    default RepositoryInstruction renameRepository(String entityName, String key) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RenameRepository);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), entityName, key));
        addStep(migrationStep);
        final RepositoryInstruction parent = this;

        return new RepositoryInstruction() {
            @Override
            public String entityName() {
                return entityName;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public void addStep(MigrationStep step) {
                parent.addStep(step);
            }
        };
    }

    default <T> RepositoryInstruction addField(String fieldName) {
        return addField(fieldName, null);
    }

    default <T> RepositoryInstruction addField(String fieldName, T defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryAddField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, defaultValue));
        addStep(migrationStep);
        return this;
    }

    default <T> RepositoryInstruction addField(String fieldName, Generator<T> generator) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryAddField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, generator));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryRenameField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), oldName, newName));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDeleteField);
        migrationStep.setArguments(new Triplet<>(entityName(), key(), fieldName));
        addStep(migrationStep);
        return this;
    }

    default <T> RepositoryInstruction changeDataType(String fieldName, TypeConverter converter) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeDataType);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, converter));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction changeIdField(String oldFieldName, String newFieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeIdField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), oldFieldName, newFieldName));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction dropIndex(String field) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndex);
        migrationStep.setArguments(new Triplet<>(entityName(), key(), field));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndices);
        migrationStep.setArguments(new Pair<>(entityName(), key()));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction createIndex(String field, String indexType) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryCreateIndex);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), field, indexType));
        addStep(migrationStep);
        return this;
    }

    String entityName();

    String key();
}
