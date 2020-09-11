package org.dizitart.no2.migration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.tuples.Triplet;
import org.dizitart.no2.common.util.SecureString;
import org.dizitart.no2.exceptions.MigrationException;
import org.dizitart.no2.migration.commands.ChangePassword;
import org.dizitart.no2.migration.commands.Command;
import org.dizitart.no2.store.DatabaseMetaData;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Anindya Chatterjee
 */
public class MigrationManager {
    private final NitriteConfig nitriteConfig;
    private final DatabaseMetaData databaseMetadata;
    private final Nitrite database;

    public MigrationManager(Nitrite nitrite) {
        this.database = nitrite;
        this.nitriteConfig = nitrite.getConfig();
        this.databaseMetadata = nitrite.getDatabaseMetaData();
    }

    public void doMigrate() {
        if (isMigrationNeeded()) {
            Integer existingVersion = databaseMetadata.getSchemaVersion();
            Integer incomingVersion = nitriteConfig.getSchemaVersion();

            Queue<Migration> migrationPath = findMigrationPath(existingVersion, incomingVersion);
            if (migrationPath == null || migrationPath.isEmpty()) {
                // close the database
                database.close();

                throw new MigrationException("failed to open database, as no migration path found from revision "
                    + databaseMetadata.getSchemaVersion() + " to " + nitriteConfig.getSchemaVersion());
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

    private boolean isMigrationNeeded() {
        Integer existingVersion = databaseMetadata.getSchemaVersion();
        Integer incomingVersion = nitriteConfig.getSchemaVersion();

        if (existingVersion == null) {
            throw new MigrationException("corrupted database, no revision information found");
        }

        if (incomingVersion == null) {
            throw new MigrationException("invalid revision provided");
        }
        return !existingVersion.equals(incomingVersion);
    }

    private Queue<Migration> findMigrationPath(int start, int end) {
        if (start == end) {
            return new LinkedList<>();
        }

        boolean migrateUp = end > start;
        return findUpMigrationPath(migrateUp, start, end);
    }

    private Queue<Migration> findUpMigrationPath(boolean upgrade,
                                                int start, int end) {
        Queue<Migration> result = new LinkedList<>();
        while (upgrade ? start < end : start > end) {
            TreeMap<Integer, Migration> targetNodes = nitriteConfig.getMigrations().get(start);
            if (targetNodes == null) {
                return null;
            }

            // keys are ordered so we can start searching from one end of them.
            Set<Integer> keySet;
            if (upgrade) {
                keySet = targetNodes.descendingKeySet();
            } else {
                keySet = targetNodes.keySet();
            }

            boolean found = false;
            for (int targetVersion : keySet) {
                final boolean shouldAddToPath;
                if (upgrade) {
                    shouldAddToPath = targetVersion <= end && targetVersion > start;
                } else {
                    shouldAddToPath = targetVersion >= end && targetVersion < start;
                }
                if (shouldAddToPath) {
                    result.offer(targetNodes.get(targetVersion));
                    start = targetVersion;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return result;
    }

    private void executeMigrationSteps(Queue<MigrationStep> migrationSteps) {
        if (migrationSteps != null) {
            int length = migrationSteps.size();
            for (int i = 0; i < length; i++) {
                MigrationStep step = migrationSteps.poll();
                executeStep(step);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void executeStep(MigrationStep step) {
        if (step != null) {
            Command command = null;
            switch (step.getInstructionType()) {
                case ChangePassword:
                    Triplet<String, SecureString, SecureString> args =
                        (Triplet<String, SecureString, SecureString>) step.getArguments();
                    command = new ChangePassword(args.getFirst(), args.getSecond(), args.getThird());
                    break;
                case DropCollection:
                    break;
                case DropRepository:
                    break;
                case Custom:
                    break;
                case CollectionRename:
                    break;
                case CollectionAddField:
                    break;
                case CollectionRenameField:
                    break;
                case CollectionDeleteField:
                    break;
                case CollectionDropIndex:
                    break;
                case CollectionDropIndices:
                    break;
                case CollectionCreateIndex:
                    break;
                case RenameEntity:
                    break;
                case RenameKey:
                    break;
                case RepositoryAddField:
                    break;
                case RepositoryRenameField:
                    break;
                case RepositoryDeleteField:
                    break;
                case RepositoryChangeDataType:
                    break;
                case RepositoryChangeIdField:
                    break;
                case RepositoryDropIndex:
                    break;
                case RepositoryDropIndices:
                    break;
                case RepositoryCreateIndex:
                    break;
            }

            if (command != null) {
                command.execute(database);
            }
        }
    }
}
