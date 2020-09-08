package org.dizitart.no2.migration;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Anindya Chatterjee
 */
public abstract class Migration {
    private final Queue<MigrationStep> migrationSteps;
    private final String startVersion;
    private final String endVersion;

    public Migration(String startVersion, String endVersion) {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
        this.migrationSteps = new LinkedList<>();
    }

    public abstract void migrate(Instruction instruction);

    public VersionInfo getVersionInfo() {
        return new VersionInfo(startVersion, endVersion);
    }

    public void execute() {
        NitriteInstruction instruction = new NitriteInstruction(migrationSteps);
        migrate(instruction);
        processStatements();
    }

    private void processStatements() {

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
