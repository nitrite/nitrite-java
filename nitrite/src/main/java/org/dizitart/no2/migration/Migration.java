package org.dizitart.no2.migration;

import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;


/**
 * Represents the database migration operation. A migration is a way to modify the structure of a database
 * from one version to another. It contains a queue of {@link MigrationStep}s that need to be executed
 * in order to migrate the database from the start version to the end version.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public abstract class Migration {
    private final Queue<MigrationStep> migrationSteps;

    /**
     * Returns the version number from which the migration is being performed.
     * 
    */
    @Getter
    private final Integer fromVersion;

    /**
     * Returns the version number to which the migration is being performed.
     */
    @Getter
    private final Integer toVersion;

    private boolean executed = false;

    /**
     * Instantiates a new {@link Migration}.
     *
     * @param fromVersion the start version
     * @param toVersion   the end version
     */
    public Migration(Integer fromVersion, Integer toVersion) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.migrationSteps = new LinkedList<>();
    }

    /**
     * Migrates the database using the <code>instructions</code>.
     *
     * @param instructionSet the instructions
     */
    public abstract void migrate(InstructionSet instructionSet);

    /**
     * Returns the queue of {@link MigrationStep}s to be executed for the migration.
     *
     * @return the queue
     */
    public Queue<MigrationStep> steps() {
        if (!executed) {
            execute();
        }
        return migrationSteps;
    }

    private void execute() {
        NitriteInstructionSet instruction = new NitriteInstructionSet(migrationSteps);
        migrate(instruction);
        this.executed = true;
    }
}
