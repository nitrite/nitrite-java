package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;

/**
 * @author Anindya Chatterjee
 */
public interface CollectionInstruction extends Composable {
    default CollectionInstruction rename(String name) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRename);
        migrationStep.setArguments(new Pair<>(collectionName(), name));
        addStep(migrationStep);
        final CollectionInstruction parent = this;

        return new CollectionInstruction() {
            @Override
            public String collectionName() {
                return name;
            }

            @Override
            public void addStep(MigrationStep step) {
                parent.addStep(step);
            }
        };
    }

    default CollectionInstruction addField(String fieldName) {
        return addField(fieldName, null);
    }

    default CollectionInstruction addField(String fieldName, Object defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionAddField);
        migrationStep.setArguments(new Triplet<>(collectionName(), fieldName, defaultValue));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction addField(String fieldName, Generator<?> generator) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionAddField);
        migrationStep.setArguments(new Triplet<>(collectionName(), fieldName, generator));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRenameField);
        migrationStep.setArguments(new Triplet<>(collectionName(), oldName, newName));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDeleteField);
        migrationStep.setArguments(new Pair<>(collectionName(), fieldName));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction dropIndex(String indexedFieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndex);
        migrationStep.setArguments(new Pair<>(collectionName(), indexedFieldName));
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndices);
        migrationStep.setArguments(collectionName());
        addStep(migrationStep);
        return this;
    }

    default CollectionInstruction createIndex(String fieldName, String indexType) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionCreateIndex);
        migrationStep.setArguments(new Triplet<>(collectionName(), fieldName, indexType));
        addStep(migrationStep);
        return this;
    }

    String collectionName();
}
