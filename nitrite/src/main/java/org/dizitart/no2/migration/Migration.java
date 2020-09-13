package org.dizitart.no2.migration;

import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Anindya Chatterjee
 */
public abstract class Migration {
    private final Queue<MigrationStep> migrationSteps;

    @Getter
    private final Integer startVersion;

    @Getter
    private final Integer endVersion;

    private boolean executed = false;

    public Migration(Integer startVersion, Integer endVersion) {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
        this.migrationSteps = new LinkedList<>();
    }

    public abstract void migrate(Instruction instruction);

    public Queue<MigrationStep> steps() {
        if (!executed) {
            execute();
        }
        return migrationSteps;
    }

    private void execute() {
        NitriteInstruction instruction = new NitriteInstruction(migrationSteps);
        migrate(instruction);
        this.executed = true;
    }
}
