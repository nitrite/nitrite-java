package org.dizitart.no2.migration;

import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;

/**
 * Represents a migration instruction set for {@link org.dizitart.no2.collection.NitriteCollection}.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface CollectionInstruction extends Instruction {
    /**
     * Adds an instruction to rename a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param name the name
     * @return the instruction
     */
    default CollectionInstruction rename(String name) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRename);
        migrationStep.setArguments(new Pair<>(collectionName(), name));
        addStep(migrationStep);
        final CollectionInstruction parent = this;

        // new instruction set for new collection
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

    /**
     * Adds an instruction to add new field to the documents of
     * a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param fieldName the field name
     * @return the collection instruction
     */
    default CollectionInstruction addField(String fieldName) {
        return addField(fieldName, null);
    }

    /**
     * Adds an instruction to add new field with a default value, into the documents of
     * a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param fieldName    the field name
     * @param defaultValue the default value
     * @return the collection instruction
     */
    default CollectionInstruction addField(String fieldName, Object defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionAddField);
        migrationStep.setArguments(new Triplet<>(collectionName(), fieldName, defaultValue));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to add new field with value generator, into the document of
     * a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param fieldName the field name
     * @param generator the generator
     * @return the collection instruction
     */
    default CollectionInstruction addField(String fieldName, Generator<?> generator) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionAddField);
        migrationStep.setArguments(new Triplet<>(collectionName(), fieldName, generator));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to rename a field to the document of
     * a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param oldName the old name
     * @param newName the new name
     * @return the collection instruction
     */
    default CollectionInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionRenameField);
        migrationStep.setArguments(new Triplet<>(collectionName(), oldName, newName));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to delete a field from the document of
     * a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param fieldName the field name
     * @return the collection instruction
     */
    default CollectionInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDeleteField);
        migrationStep.setArguments(new Pair<>(collectionName(), fieldName));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to drop an index from a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param indexedFieldNames the indexed field names
     * @return the collection instruction
     */
    default CollectionInstruction dropIndex(String... indexedFieldNames) {
        Fields indexedFields = Fields.withNames(indexedFieldNames);
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndex);
        migrationStep.setArguments(new Pair<>(collectionName(), indexedFields));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to drop all indices from a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @return the collection instruction
     */
    default CollectionInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionDropIndices);
        migrationStep.setArguments(collectionName());
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to create an index in a {@link org.dizitart.no2.collection.NitriteCollection}.
     *
     * @param fieldNames the field names
     * @param indexType the index type
     * @return the collection instruction
     */
    default CollectionInstruction createIndex(String indexType, String... fieldNames) {
        Fields indexedFields = Fields.withNames(fieldNames);
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.CollectionCreateIndex);
        migrationStep.setArguments(new Triplet<>(collectionName(), indexedFields, indexType));
        addStep(migrationStep);
        return this;
    }

    /**
     * The name of the collection for this instruction.
     *
     * @return the name
     */
    String collectionName();
}
