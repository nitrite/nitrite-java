package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;

/**
 * @author Anindya Chatterjee
 */
public interface RepositoryInstruction extends Composable {

    default RepositoryInstruction renameEntity(String entityName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RenameEntity);
        migrationStep.setArguments(entityName);
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction renameKey(String key) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RenameKey);
        migrationStep.setArguments(key);
        addStep(migrationStep);
        return this;
    }

    default <T> RepositoryInstruction addField(String fieldName, Class<T> type) {
        return addField(fieldName, type, null);
    }

    default <T> RepositoryInstruction addField(String fieldName, Class<T> type, T defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryAddField);
        migrationStep.setArguments(new Triplet<>(fieldName, type, defaultValue));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryRenameField);
        migrationStep.setArguments(new Pair<>(oldName, newName));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDeleteField);
        migrationStep.setArguments(fieldName);
        addStep(migrationStep);
        return this;
    }

    default <T> RepositoryInstruction changeDataType(String fieldName, Class<T> newType) {
        return changeDataType(fieldName, newType, null);
    }

    default <T> RepositoryInstruction changeDataType(String fieldName, Class<T> newType, T defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeDataType);
        migrationStep.setArguments(new Triplet<>(fieldName, newType, defaultValue));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction changeIdField(String oldFieldName, String newFieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeIdField);
        migrationStep.setArguments(new Pair<>(oldFieldName, newFieldName));
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction dropIndex(String field) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndex);
        migrationStep.setArguments(field);
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndices);
        migrationStep.setArguments(null);
        addStep(migrationStep);
        return this;
    }

    default RepositoryInstruction createIndex(String field, String indexType) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryCreateIndex);
        migrationStep.setArguments(new Pair<>(field, indexType));
        addStep(migrationStep);
        return this;
    }
}
