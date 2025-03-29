package org.dizitart.no2.migration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Quartet;
import org.dizitart.no2.common.tuples.Triplet;
import org.dizitart.no2.common.util.SecureString;
import org.dizitart.no2.exceptions.MigrationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.migration.commands.*;
import org.dizitart.no2.store.StoreMetaData;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.UserAuthenticationService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import static org.dizitart.no2.common.Constants.STORE_INFO;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class MigrationManager {
    private final NitriteConfig nitriteConfig;
    private final StoreMetaData storeMetadata;
    private final Nitrite database;

    /**
     * Instantiates a new {@link MigrationManager}.
     *
     * @param nitrite the nitrite database
     */
    public MigrationManager(Nitrite nitrite) {
        this.database = nitrite;
        this.nitriteConfig = nitrite.getConfig();
        this.storeMetadata = nitrite.getDatabaseMetaData();
    }

    /**
     * Performs the migration on the database.
     */
    public void doMigrate() {
        if (isMigrationNeeded()) {
            Integer currentVersion = storeMetadata.getSchemaVersion();
            Integer targetVersion = nitriteConfig.getSchemaVersion();

            Queue<Migration> migrationPath = findMigrationPath(currentVersion, targetVersion);
            boolean hasValidPath = migrationPath != null && !migrationPath.isEmpty();

            if (!hasValidPath) {
                closeDatabaseAndThrowException( currentVersion, targetVersion);
            }
            executeMigrationPath(migrationPath);
        }
    }

    private void closeDatabaseAndThrowException(Integer currentVersion, Integer targetVersion) {
        // close the database
        try {
            database.close();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to close the database", e);
        }
        throw new MigrationException("Schema version mismatch, no migration path found from version "
            + currentVersion + " to " + targetVersion);
    }

    private void executeMigrationPath(Queue<Migration> migrationPath) {
        int pathLength = migrationPath.size();
        for (int i = 0; i < pathLength; i++) {
            Migration migration = migrationPath.poll();
            if (migration != null) {
                Queue<MigrationStep> migrationSteps = migration.steps();
                executeMigrationSteps(migrationSteps);
            }
        }
    }

    private boolean isMigrationNeeded() {
        Integer existingVersion = storeMetadata.getSchemaVersion();
        Integer incomingVersion = nitriteConfig.getSchemaVersion();

        if (existingVersion == null) {
            throw new MigrationException("Corrupted database, no version information found");
        }

        if (incomingVersion == null) {
            throw new MigrationException("Invalid version provided");
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

    private Queue<Migration> findUpMigrationPath(boolean upgrade, int start, int end) {
        Queue<Migration> result = new LinkedList<>();
        while (upgrade ? start < end : start > end) {
            TreeMap<Integer, Migration> targetNodes = nitriteConfig.getMigrations().get(start);
            if (targetNodes == null) {
                return null;
            }

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

        StoreMetaData metaData = database.getDatabaseMetaData();
        metaData.setSchemaVersion(nitriteConfig.getSchemaVersion());

        NitriteMap<String, Document> storeInfo = database.getStore().openMap(STORE_INFO,
            String.class, Document.class);
        storeInfo.put(STORE_INFO, metaData.getInfo());
    }

    @SuppressWarnings("unchecked")
    private void executeStep(MigrationStep step) {
        if (step != null) {
            Command command = null;
            switch (step.getInstructionType()) {
                case AddUser:
                    Pair<String, SecureString> arg1 =
                        (Pair<String, SecureString>) step.getArguments();
                    command = nitrite -> {
                        UserAuthenticationService authService = new UserAuthenticationService(nitrite.getStore());
                        authService.addOrUpdatePassword(false, arg1.getFirst(),
                            null, arg1.getSecond());
                    };
                    break;

                case ChangePassword:
                    Triplet<String, SecureString, SecureString> arg2 =
                        (Triplet<String, SecureString, SecureString>) step.getArguments();
                    command = nitrite -> {
                        UserAuthenticationService authService = new UserAuthenticationService(nitrite.getStore());
                        authService.addOrUpdatePassword(true, arg2.getFirst(), arg2.getSecond(), arg2.getThird());
                    };
                    break;

                case DropCollection:
                    String arg3 = (String) step.getArguments();
                    command = new Drop(arg3);
                    break;

                case DropRepository:
                    Pair<String, String> arg4 = (Pair<String, String>) step.getArguments();
                    command = new Drop(findRepositoryName(arg4.getFirst(), arg4.getSecond()));
                    break;

                case Custom:
                    CustomInstruction instruction = (CustomInstruction) step.getArguments();
                    command = instruction::perform;
                    break;

                case CollectionRename:
                    Pair<String, String> arg5 = (Pair<String, String>) step.getArguments();
                    command = new Rename(arg5.getFirst(), arg5.getSecond());
                    break;

                case CollectionAddField:
                    Triplet<String, String, Object> arg6 = (Triplet<String, String, Object>) step.getArguments();
                    command = new AddField(arg6.getFirst(), arg6.getSecond(), arg6.getThird());
                    break;

                case CollectionRenameField:
                    Triplet<String, String, String> arg7 = (Triplet<String, String, String>) step.getArguments();
                    command = new RenameField(arg7.getFirst(), arg7.getSecond(), arg7.getThird());
                    break;

                case CollectionDeleteField:
                    Pair<String, String> arg8 = (Pair<String, String>) step.getArguments();
                    command = new DeleteField(arg8.getFirst(), arg8.getSecond());
                    break;

                case CollectionDropIndex:
                    Pair<String, Fields> arg9 = (Pair<String, Fields>) step.getArguments();
                    command = new DropIndex(arg9.getFirst(), arg9.getSecond());
                    break;

                case CollectionDropIndices:
                    String collectionName = (String) step.getArguments();
                    command = new DropIndex(collectionName, null);
                    break;

                case CollectionCreateIndex:
                    Triplet<String, Fields, String> arg10 = (Triplet<String, Fields, String>) step.getArguments();
                    command = new CreateIndex(arg10.getFirst(), arg10.getSecond(), arg10.getThird());
                    break;

                case RenameRepository:
                    Quartet<String, String, String, String> arg11 = (Quartet<String, String, String, String>) step.getArguments();
                    String repositoryName = findRepositoryName(arg11.getFirst(), arg11.getSecond());
                    String newRepositoryName = findRepositoryName(arg11.getThird(), arg11.getFourth());
                    command = new Rename(repositoryName, newRepositoryName);
                    break;

                case RepositoryAddField:
                    Quartet<String, String, String, Object> arg13
                        = (Quartet<String, String, String, Object>) step.getArguments();
                    command = new AddField(findRepositoryName(arg13.getFirst(), arg13.getSecond()),
                        arg13.getThird(), arg13.getFourth());
                    break;

                case RepositoryRenameField:
                    Quartet<String, String, String, String> arg14 =
                        (Quartet<String, String, String, String>) step.getArguments();
                    command = new RenameField(findRepositoryName(arg14.getFirst(), arg14.getSecond()),
                        arg14.getThird(), arg14.getFourth());
                    break;

                case RepositoryDeleteField:
                    Triplet<String, String, String> arg15 = (Triplet<String, String, String>) step.getArguments();
                    command = new DeleteField(findRepositoryName(arg15.getFirst(), arg15.getSecond()),
                        arg15.getThird());
                    break;

                case RepositoryChangeDataType:
                    Quartet<String, String, String, TypeConverter<?, ?>> arg16 =
                        (Quartet<String, String, String, TypeConverter<?, ?>>) step.getArguments();
                    command = new ChangeDataType(findRepositoryName(arg16.getFirst(), arg16.getSecond()),
                        arg16.getThird(), arg16.getFourth());
                    break;

                case RepositoryChangeIdField:
                    Quartet<String, String, Fields, Fields> arg17 =
                        (Quartet<String, String, Fields, Fields>) step.getArguments();
                    command = new ChangeIdField(findRepositoryName(arg17.getFirst(), arg17.getSecond()),
                        arg17.getThird(), arg17.getFourth());
                    break;

                case RepositoryDropIndex:
                    Triplet<String, String, Fields> arg18 = (Triplet<String, String, Fields>) step.getArguments();
                    command = new DropIndex(findRepositoryName(arg18.getFirst(), arg18.getSecond()), arg18.getThird());
                    break;

                case RepositoryDropIndices:
                    Pair<String, String> arg19 = (Pair<String, String>) step.getArguments();
                    command = new DropIndex(findRepositoryName(arg19.getFirst(), arg19.getSecond()), null);
                    break;

                case RepositoryCreateIndex:
                    Quartet<String, String, Fields, String> arg20 =
                        (Quartet<String, String, Fields, String>) step.getArguments();
                    command = new CreateIndex(findRepositoryName(arg20.getFirst(), arg20.getSecond()),
                        arg20.getThird(), arg20.getFourth());
                    break;
            }

            command.execute(database);
            command.close();
        }
    }
}
