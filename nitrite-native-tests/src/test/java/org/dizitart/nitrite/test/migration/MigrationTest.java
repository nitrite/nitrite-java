package org.dizitart.nitrite.test.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dizitart.no2.index.IndexOptions.indexOptions;

import java.io.IOException;

import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.migration.InstructionSet;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.jupiter.api.Test;

public class MigrationTest {

    @Test
    void schemaMigrationShouldSucceed() throws IOException {

        final MVStoreModule mvStoreModule = MVStoreModule.withConfig()
            .filePath(DatabaseTestUtils.getRandomDatabasePath().toAbsolutePath().toFile())
            .build();

        try (final Nitrite database = Nitrite.builder()
            .loadModule(mvStoreModule)
            .schemaVersion(1)
            .openOrCreate()) {

            final var collection = database.getCollection("test");
            for (int i = 0; i < 10; i++) {
                Document document = Document.createDocument();
                document.put("firstName", "first-name");
                document.put("lastName", "last-name");
                document.put("bloodGroup", "blood-group");
                document.put("age", 21);

                collection.insert(document);
            }

            collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName");
            collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");

            assertThat(collection.hasIndex("firstName")).isTrue();
            assertThat(collection.hasIndex("lastName")).isTrue();
            assertThat(collection.size()).isEqualTo(10);
            assertThat(database.listCollectionNames()).hasSize(1);
            assertThat(database.getDatabaseMetaData().getSchemaVersion()).isEqualTo(Constants.INITIAL_SCHEMA_VERSION);
        }

        final Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, Constants.INITIAL_SCHEMA_VERSION + 1) {
            @Override
            public void migrate(InstructionSet instruction) {
                instruction.forDatabase()
                    .addUser("test-user", "test-password");

                instruction.forCollection("test")
                    .rename("testCollectionMigrate")
                    .deleteField("lastName");
            }
        };

        try (final Nitrite database = Nitrite.builder()
            .loadModule(mvStoreModule)
            .schemaVersion(Constants.INITIAL_SCHEMA_VERSION + 1)
            .addMigrations(migration)
            .openOrCreate("test-user", "test-password")) {

            final var collection = database.getCollection("testCollectionMigrate");
            assertThat(collection.hasIndex("firstName")).isTrue();
            assertThat(collection.hasIndex("lastName")).isFalse();
            assertThat(collection.size()).isEqualTo(10);
            assertThat(database.listCollectionNames()).hasSize(1);
            assertThat(database.getDatabaseMetaData().getSchemaVersion()).isEqualTo(2);
        }
    }
}
