package org.dizitart.no2.migrate;

import com.github.javafaker.Faker;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.migration.Instruction;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.migration.TypeConverter;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.TestUtil.createDb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class MigrationTest {
    private final String dbPath = getRandomTempDbFile();
    private Nitrite db;

//    @Rule
//    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        Faker faker = new Faker();
        db = createDb(dbPath);

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
    }

    @After
    public void cleanUp() throws IOException {
        if (!db.isClosed()) {
            db.close();
        }

        Files.delete(Paths.get(dbPath));
    }

    @Test
    public void testMigrate() {
        Migration migration = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
            @Override
            public void migrate(Instruction instruction) {
                instruction.forDatabase()
                    .addPassword("test-user", "test-password");

                instruction.forRepository(OldClass.class, "demo1")
                    .renameRepository("new", null)
                    .changeDataType("empId", (TypeConverter<String, Long>) Long::parseLong)
                    .changeIdField("uuid", "empId")
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
}
