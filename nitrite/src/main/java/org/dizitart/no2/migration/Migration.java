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
    private final Integer fromVersion;

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
