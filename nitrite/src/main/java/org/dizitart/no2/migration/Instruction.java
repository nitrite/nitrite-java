package org.dizitart.no2.migration;

/**
 * Represents a collection of database migration steps.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
interface Instruction {
    /**
     * Adds a migration step to the instruction set.
     *
     * @param step the step
     */
    void addStep(MigrationStep step);
}
