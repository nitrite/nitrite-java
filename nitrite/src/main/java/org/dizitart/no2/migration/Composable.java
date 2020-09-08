package org.dizitart.no2.migration;

/**
 * @author Anindya Chatterjee
 */
interface Composable {
    void addStep(MigrationStep step);
}
