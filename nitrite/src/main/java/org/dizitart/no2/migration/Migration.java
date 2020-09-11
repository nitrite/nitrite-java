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

    /*
     * Nitrite.builder()
     * .version("2.1")
     * .addMigrations(mg1, mg2)
     * .createOrOpen();
     *
     *
     * public class EmployeeMigration implement Migration {
     *
     *      public void migrate(Instruction instruction) {
     *          instruction.forDatabase()
     *              .changePassword("user", "pass1", "pass2")
     *              .dropCollection("junk")
     *              .dropRepository("com.demo.Junk")
     *
     *          instruction.forRepository("org.demo.Employee")
     *              .renameEntity("com.demo.Employee2")
     *              .addField("age", Integer.class)
     *              .renameField("name", "firstName")
     *              .deleteField("company")
     *              .changeDataType("address.line1", String.class)
     *              .changeIdField("empId", "firstName")
     *              .dropIndex("name")
     *              .createIndex("age", IndexType.NonUnique)
     *
     *          instruction.forCollection("test")
     *              .rename("test2")
     *              .addField("age", Integer.class)
     *              .renameField("name", "firstName")
     *              .deleteField("company")
     *              .dropIndex("date")
     *              .createIndex("firstName")
     *      }
     * }
     *
     *
     *
     * */
}
