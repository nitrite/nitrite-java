package org.dizitart.nitrite.test;

import org.dizitart.nitrite.test.repository.PersonEntityConverter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

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

    public static Nitrite setupDatabase(final URL template) throws IOException {

        final Path databasePath = getRandomDatabasePath();

        if (template != null) {
            logger.finest("Loading template from '" + template + "'.");
            try (final InputStream is = template.openStream()) {
                Files.copy(is, databasePath, StandardCopyOption.REPLACE_EXISTING);
            }
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
