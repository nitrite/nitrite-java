package org.dizitart.no2.migration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.MigrationException;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreMetadata;

import java.util.Queue;

/**
 * @author Anindya Chatterjee
 */
public class MigrationManager {
    private final NitriteConfig nitriteConfig;
    private final NitriteStore<?> nitriteStore;
    private final StoreMetadata storeMetadata;
    private final Nitrite database;


    public MigrationManager(Nitrite nitrite) {
        this.database = nitrite;
        this.nitriteConfig = nitrite.getConfig();
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.storeMetadata = nitriteStore.getStoreInfo();
    }

    public void doMigrate() {
        /*
        * Scenarios
        *
        * 1. Version 3.x (no store-info) to Version 4.x
        * 2. No revision in new Version 4.x
        * 3. Introduced first version in Version 4.x
        * 4. Upgrade version
        *
        * */

        if (!storeMetadata.getDatabaseRevision().equalsIgnoreCase(nitriteConfig.getRevision())) {
            Queue<Migration> migrationPath = calculateMigrationPath(storeMetadata.getDatabaseRevision(), nitriteConfig);
            if (migrationPath == null || migrationPath.isEmpty()) {
                // close the database
                database.close();

                throw new MigrationException("failed to open database, as no migration path found from revision "
                    + storeMetadata.getDatabaseRevision() + " to " + nitriteConfig.getRevision());
            }

            int length = migrationPath.size();
            for (int i = 0; i < length; i++) {
                Migration migration = migrationPath.poll();
                if (migration != null) {
                    Queue<MigrationStep> migrationSteps = migration.steps();
                    executeMigrationSteps(migrationSteps);
                }
            }
        }
    }

    private boolean isMigrationNeeded(Nitrite nitrite) {
        return false;
    }

    private Queue<Migration> calculateMigrationPath(String revision, NitriteConfig nitriteConfig) {
        return null;
    }

    private void executeMigrationSteps(Queue<MigrationStep> migrationSteps) {

    }
}
