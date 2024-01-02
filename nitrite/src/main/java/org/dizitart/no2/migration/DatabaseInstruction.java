package org.dizitart.no2.migration;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;
import org.dizitart.no2.common.util.SecureString;
import org.dizitart.no2.repository.EntityDecorator;

import static org.dizitart.no2.common.util.ObjectUtils.getEntityName;

/**
 * Represents a migration instruction set for the nitrite database.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface DatabaseInstruction extends Instruction {

    /**
     * Adds an instruction to set a user authentication to the database.
     *
     * @param username the username
     * @param password the password
     * @return the database instruction
     */
    default DatabaseInstruction addUser(String username, String password) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.AddUser);
        migrationStep.setArguments(new Pair<>(username, new SecureString(password)));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to change the password for the user authentication to the database.
     *
     * @param username    the username
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return the database instruction
     */
    default DatabaseInstruction changePassword(String username, String oldPassword, String newPassword) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.ChangePassword);
        migrationStep.setArguments(new Triplet<>(username, new SecureString(oldPassword), new SecureString(newPassword)));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to drop a {@link org.dizitart.no2.collection.NitriteCollection} from the database.
     *
     * @param collectionName the collection name
     * @return the database instruction
     */
    default DatabaseInstruction dropCollection(String collectionName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.DropCollection);
        migrationStep.setArguments(collectionName);
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to drop an {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param type the type
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(Class<?> type) {
        return dropRepository(type, null);
    }

    /**
     * Adds an instruction to drop a keyed {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param type the type
     * @param key  the key
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(Class<?> type, String key) {
        return dropRepository(getEntityName(type), key);
    }

    /**
     * Adds an instruction to drop an {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param entityDecorator the entityDecorator
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(EntityDecorator<?> entityDecorator) {
        return dropRepository(entityDecorator, null);
    }

    /**
     * Adds an instruction to drop an {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param entityDecorator the entityDecorator
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(EntityDecorator<?> entityDecorator, String key) {
        return dropRepository(entityDecorator.getEntityName(), key);
    }

    /**
     * Adds an instruction to drop an {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param typeName the type name
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(String typeName) {
        return dropRepository(typeName, null);
    }

    /**
     * Adds an instruction to drop a keyed {@link org.dizitart.no2.repository.ObjectRepository} from the database.
     *
     * @param typeName the type name
     * @param key      the key
     * @return the database instruction
     */
    default DatabaseInstruction dropRepository(String typeName, String key) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.DropRepository);
        migrationStep.setArguments(new Pair<>(typeName, key));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds a custom instruction to perform a user defined operation on the database.
     *
     * @param instruction the instruction
     * @return the database instruction
     */
    default DatabaseInstruction customInstruction(CustomInstruction instruction) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.Custom);
        migrationStep.setArguments(instruction);
        addStep(migrationStep);
        return this;
    }
}
