/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.migrate;

import com.github.javafaker.Faker;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.exceptions.MigrationException;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.migration.Instructions;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.migration.TypeConverter;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.integration.repository.Retry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.dizitart.no2.integration.repository.BaseObjectRepositoryTest.getRandomTempDbFile;
import static org.dizitart.no2.integration.repository.TestUtil.createDb;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class MigrationTest {
    private final String dbPath = getRandomTempDbFile();
    private Nitrite db;
    private Faker faker;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        db = createDb(dbPath, new JacksonMapperModule());
        faker = new Faker();
    }

    @After
    public void cleanUp() throws IOException {
        if (!db.isClosed()) {
            db.close();
        }

        Files.delete(Paths.get(dbPath));
    }

    @Test
    public void testRepositoryMigrate() {
        ObjectRepository<OldClass> oldRepo = db.getRepository(OldClass.class, "demo1");
        for (int i = 0; i < 10; i++) {
            OldClass old = new OldClass();
            old.setEmpId(String.valueOf(faker.number().randomNumber()));
            old.setFirstName(faker.name().firstName());
            old.setLastName(faker.name().lastName());
            old.setUuid(UUID.randomUUID().toString());

            OldClass.Literature literature = new OldClass.Literature();
            literature.setRatings((float) faker.number().randomDouble(2, 1, 5));
            literature.setText(faker.lorem().paragraph());
            old.setLiterature(literature);

            oldRepo.insert(old);
        }

        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instructions instruction) {
                instruction.forDatabase()
                    .addPassword("test-user", "test-password");

                instruction.forRepository(OldClass.class, "demo1")
                    .renameRepository("new", null)
                    .changeDataType("empId", (TypeConverter<String, Long>) Long::parseLong)
                    .changeIdField(Fields.withNames("uuid"), Fields.withNames("empId"))
                    .deleteField("uuid")
                    .renameField("lastName", "familyName")
                    .addField("fullName", document -> document.get("firstName", String.class) + " "
                        + document.get("familyName", String.class))
                    .dropIndex("firstName")
                    .dropIndex("literature.text")
                    .changeDataType("literature.ratings", (TypeConverter<Float, Integer>) Math::round);
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compress(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate("test-user", "test-password");

        ObjectRepository<NewClass> newRepo = db.getRepository(NewClass.class);
        assertEquals(newRepo.size(), 10);
        assertTrue(db.listCollectionNames().isEmpty());
        assertTrue(db.listKeyedRepository().isEmpty());

        assertEquals((int) db.getDatabaseMetaData().getSchemaVersion(), 2);
    }

    @Test
    public void testCollectionMigrate() {
        NitriteCollection collection = db.getCollection("test");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());
            document.put("bloodGroup", faker.name().bloodGroup());
            document.put("age", faker.number().randomDigit());

            collection.insert(document);
        }

        collection.createIndex(indexOptions(IndexType.NonUnique), "firstName");
        collection.createIndex(indexOptions(IndexType.NonUnique), "bloodGroup");
        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instructions instruction) {
                instruction.forDatabase()
                    .addPassword("test-user", "test-password");

                instruction.forCollection("test")
                    .rename("testCollectionMigrate")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate("test-user", "test-password");

        collection = db.getCollection("testCollectionMigrate");
        assertTrue(collection.hasIndex("firstName"));
        assertEquals(collection.size(), 10);
        assertEquals(db.listCollectionNames().size(), 1);
        assertEquals((int) db.getDatabaseMetaData().getSchemaVersion(), 2);
        db.close();

        migration = new Migration(2, 3) {
            @Override
            public void migrate(Instructions instructions) {
                instructions.forDatabase()
                    .changePassword("test-user", "test-password", "password");

                instructions.forCollection("testCollectionMigrate")
                    .dropIndex("firstName")
                    .deleteField("bloodGroup")
                    .addField("name", document -> faker.name().fullName())
                    .addField("address")
                    .addField("vehicles", 1)
                    .renameField("age", "ageGroup")
                    .createIndex(IndexType.NonUnique, "ageGroup");
            }
        };

        storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(3)
            .addMigrations(migration)
            .openOrCreate("test-user", "password");

        collection = db.getCollection("testCollectionMigrate");
        assertEquals(collection.size(), 10);
        assertEquals(db.listCollectionNames().size(), 1);
        assertEquals((int) db.getDatabaseMetaData().getSchemaVersion(), 3);

        assertFalse(collection.hasIndex("firstName"));
        assertTrue(collection.hasIndex("ageGroup"));
        assertEquals(collection.find(where("age").notEq(null)).size(), 0);
    }

    @Test(expected = MigrationException.class)
    public void testOpenWithoutSchemaVersion() {
        NitriteCollection collection = db.getCollection("test");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("test")
                    .rename("testOpenWithoutSchemaVersion")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testOpenWithoutSchemaVersion");
        assertEquals(collection.size(), 10);
        db.close();

        storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();

        collection = db.getCollection("testOpenWithoutSchemaVersion");
        assertEquals(collection.size(), 10);
    }

    @Test
    public void testDescendingSchema() {
        NitriteCollection collection = db.getCollection("test");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("test")
                    .rename("testDescendingSchema")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testDescendingSchema");
        assertEquals(collection.size(), 10);
        db.close();

        migration = new Migration(2, Constants.INITIAL_SCHEMA_VERSION) {
            @Override
            public void migrate(Instructions instructions) {

                instructions.forCollection("testDescendingSchema")
                    .rename("test");
            }
        };

        storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(1)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();
    }

    @Test
    public void testMigrationWithoutVersion() {
        NitriteCollection collection = db.getCollection("test");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("test")
                    .rename("testMigrationWithoutVersion")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testMigrationWithoutVersion");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();
    }

    @Test
    public void testWrongSchemaVersionNoMigration() {
        NitriteCollection collection = db.getCollection("testWrongSchemaVersionNoMigration");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration = new Migration(1, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("testWrongSchemaVersionNoMigration")
                    .rename("test")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testWrongSchemaVersionNoMigration");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();

        migration = new Migration(2, 3) {
            @Override
            public void migrate(Instructions instructions) {
                instructions.forCollection("test")
                    .rename("testWrongSchemaVersionNoMigration");
            }
        };

        storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testWrongSchemaVersionNoMigration");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();
    }

    @Test
    public void testReOpenAfterMigration() {
        NitriteCollection collection = db.getCollection("testReOpenAfterMigration");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration = new Migration(1, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("testReOpenAfterMigration")
                    .rename("test")
                    .deleteField("lastName");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testReOpenAfterMigration");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate();

        collection = db.getCollection("testReOpenAfterMigration");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        db.close();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .openOrCreate();

        collection = db.getCollection("testReOpenAfterMigration");
        assertEquals(collection.size(), 0);

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
    }

    @Test
    public void testMultipleMigrations() {
        NitriteCollection collection = db.getCollection("testMultipleMigrations");
        for (int i = 0; i < 10; i++) {
            Document document = Document.createDocument();
            document.put("firstName", faker.name().firstName());
            document.put("lastName", faker.name().lastName());

            collection.insert(document);
        }
        db.close();

        Migration migration1 = new Migration(1, 2) {
            @Override
            public void migrate(Instructions instruction) {

                instruction.forCollection("testMultipleMigrations")
                    .rename("test");
            }
        };

        Migration migration2 = new Migration(2, 3) {
            @Override
            public void migrate(Instructions instruction) {
                instruction.forCollection("test")
                    .addField("fullName", "Dummy Name");
            }
        };

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration1, migration2)
            .openOrCreate();

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        assertEquals(collection.find(where("fullName").eq("Dummy Name")).size(), 0);
        db.close();

        Migration migration3 = new Migration(3, 4) {
            @Override
            public void migrate(Instructions instruction) {
                instruction.forCollection("test")
                    .addField("age", 10);
            }
        };

        storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compressHigh(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(4)
            .addMigrations(migration1, migration2, migration3)
            .openOrCreate();

        collection = db.getCollection("test");
        assertEquals(collection.size(), 10);
        assertEquals(collection.find(where("fullName").eq("Dummy Name")).size(), 10);
        assertEquals(collection.find(where("age").eq(10)).size(), 10);
    }
}
