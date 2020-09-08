package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;

/**
 * @author Anindya Chatterjee
 */
public interface CollectionInstruction extends Composable {
    default CollectionInstruction rename(String name) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRename);
        migrationStep.setArguments(name);
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction addField(String fieldName, Object defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionAddField);
        migrationStep.setArguments(new Pair<>(fieldName, defaultValue));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRenameField);
        migrationStep.setArguments(new Pair<>(oldName, newName));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDeleteField);
        migrationStep.setArguments(fieldName);
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction dropIndex(String indexedFieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndex);
        migrationStep.setArguments(indexedFieldName);
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndices);
        migrationStep.setArguments(null);
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction createIndex(String fieldName, String indexType) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionCreateIndex);
        migrationStep.setArguments(new Pair<>(fieldName, indexType));
        addStep(migrationStep);
        return this;
    }
}
