package org.dizitart.no2.migration;

import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents the database migration operation.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public abstract class Migration {
    private final Queue<MigrationStep> migrationSteps;

    @Getter
    private final Integer startVersion;

    @Getter
    private final Integer endVersion;

    private boolean executed = false;

    /**
     * Instantiates a new {@link Migration}.
     *
     * @param startVersion the start version
     * @param endVersion   the end version
     */
    public Migration(Integer startVersion, Integer endVersion) {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
        this.migrationSteps = new LinkedList<>();
    }

    /**
     * Migrates the database using the <code>instructions</code>.
     *
     * @param instructions the instructions
     */
    public abstract void migrate(Instructions instructions);

    /**
     * Returns the {@link MigrationStep}s as a queue for execution.
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
        NitriteInstructions instruction = new NitriteInstructions(migrationSteps);
        migrate(instruction);
        this.executed = true;
    }
}
