package org.dizitart.nitrite.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import org.dizitart.nitrite.test.repository.PersonEntityConverter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;

public class DatabaseTestUtils {

    private static final Logger logger = Logger.getLogger(DatabaseTestUtils.class.getSimpleName());

    public static Path getRandomDatabasePath() throws IOException {
        final Path databasePath = Files.createTempFile("nitrite-native", ".db");
        logger.finest("Test Db location: " + databasePath.toAbsolutePath());
        return databasePath;
    }

    public static Nitrite setupDatabase() throws IOException {
        return setupDatabase(null);
    }

    public static Nitrite setupDatabase(final Path template) throws IOException {

        final Path databasePath = getRandomDatabasePath();

        if (template != null) {
            logger.finest("Loading template from '" + template.toAbsolutePath() + "'.");
            Files.copy(template, databasePath, StandardCopyOption.REPLACE_EXISTING);
        }

        final MVStoreModule mvStoreModule = MVStoreModule.withConfig()
            .filePath(databasePath.toFile())
            .build();

        return Nitrite.builder()
            .loadModule(mvStoreModule)
            .disableRepositoryTypeValidation()
            .registerEntityConverter(new PersonEntityConverter())
            .openOrCreate("test", "test");
    }
}
