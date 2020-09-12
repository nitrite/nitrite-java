package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;
import org.dizitart.no2.common.util.SecureString;

/**
 * @author Anindya Chatterjee
 */
public interface DatabaseInstruction extends Composable {

    default DatabaseInstruction addPassword(String username, String password) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.AddPassword);
        migrationStep.setArguments(new Pair<>(username, new SecureString(password)));
        addStep(migrationStep);
        return this;
    }

    default DatabaseInstruction changePassword(String username, String oldPassword, String newPassword) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.ChangePassword);
        migrationStep.setArguments(new Triplet<>(username, new SecureString(oldPassword), new SecureString(newPassword)));
        addStep(migrationStep);
        return this;
    }

    default DatabaseInstruction dropCollection(String collectionName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.DropCollection);
        migrationStep.setArguments(collectionName);
        addStep(migrationStep);
        return this;
    }

    default DatabaseInstruction dropRepository(Class<?> type) {
        return dropRepository(type.getName());
    }

    default DatabaseInstruction dropRepository(String typeName) {
        return dropRepository(typeName, null);
    }

    default DatabaseInstruction dropRepository(Class<?> type, String key) {
        return dropRepository(type.getName(), key);
    }

    default DatabaseInstruction dropRepository(String typeName, String key) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.DropRepository);
        migrationStep.setArguments(new Pair<>(typeName, key));
        addStep(migrationStep);
        return this;
    }

    default DatabaseInstruction customInstruction(CustomInstruction instruction) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.Custom);
        migrationStep.setArguments(instruction);
        addStep(migrationStep);
        return this;
    }
}
