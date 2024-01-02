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
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.exceptions.MigrationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.migration.InstructionSet;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.migration.TypeConverter;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.*;
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
        db = createDb(dbPath);
        SimpleNitriteMapper documentMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new OldClass.Converter());
        documentMapper.registerEntityConverter(new OldClass.Literature.Converter());
        documentMapper.registerEntityConverter(new NewClass.Converter());
        documentMapper.registerEntityConverter(new NewClass.Literature.Converter());

        faker = new Faker();
    }

    @After
    public void cleanUp() {
        if (!db.isClosed()) {
            db.close();
        }

        deleteDb(dbPath);
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
            public void migrate(InstructionSet instruction) {
                instruction.forDatabase()
                    .addUser("test-user", "test-password");

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

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .schemaVersion(2)
            .addMigrations(migration)
            .openOrCreate("test-user", "test-password");

        SimpleNitriteMapper documentMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new OldClass.Converter());
        documentMapper.registerEntityConverter(new OldClass.Literature.Converter());
        documentMapper.registerEntityConverter(new NewClass.Converter());
        documentMapper.registerEntityConverter(new NewClass.Literature.Converter());

        ObjectRepository<NewClass> newRepo = db.getRepository(NewClass.class);
        assertEquals(newRepo.size(), 10);
        assertTrue(db.listCollectionNames().isEmpty());
        assertTrue(db.listKeyedRepositories().isEmpty());

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

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "firstName");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "bloodGroup");
        db.close();

        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(InstructionSet instruction) {
                instruction.forDatabase()
                    .addUser("test-user", "test-password");

                instruction.forCollection("test")
                    .rename("testCollectionMigrate")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instructionSet) {
                instructionSet.forDatabase()
                    .changePassword("test-user", "test-password", "password");

                instructionSet.forCollection("testCollectionMigrate")
                    .dropIndex("firstName")
                    .deleteField("bloodGroup")
                    .addField("name", document -> faker.name().fullName())
                    .addField("address")
                    .addField("vehicles", 1)
                    .renameField("age", "ageGroup")
                    .createIndex(IndexType.NON_UNIQUE, "ageGroup");
            }
        };

        storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("test")
                    .rename("testOpenWithoutSchemaVersion")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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

        storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("test")
                    .rename("testDescendingSchema")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instructionSet) {

                instructionSet.forCollection("testDescendingSchema")
                    .rename("test");
            }
        };

        storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("test")
                    .rename("testMigrationWithoutVersion")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("testWrongSchemaVersionNoMigration")
                    .rename("test")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instructionSet) {
                instructionSet.forCollection("test")
                    .rename("testWrongSchemaVersionNoMigration");
            }
        };

        storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("testReOpenAfterMigration")
                    .rename("test")
                    .deleteField("lastName");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {

                instruction.forCollection("testMultipleMigrations")
                    .rename("test");
            }
        };

        Migration migration2 = new Migration(2, 3) {
            @Override
            public void migrate(InstructionSet instruction) {
                instruction.forCollection("test")
                    .addField("fullName", "Dummy Name");
            }
        };

        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
            public void migrate(InstructionSet instruction) {
                instruction.forCollection("test")
                    .addField("age", 10);
            }
        };

        storeModule = RocksDBModule.withConfig()
            .filePath(dbPath)
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
