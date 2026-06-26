package org.dizitart.no2.mapper.jackson.integration.repository;

import static org.dizitart.no2.mapper.jackson.integration.repository.BaseObjectRepositoryTest.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class JacksonModuleTest {
    private Nitrite db;
    private final String fileName = getRandomTempDbFile();

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        TestUtil.deleteDb(fileName);
    }

    @Test
    public void testJavaTimeModule() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(fileName)
            .build();

        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .loadModule(new JacksonMapperModule())
            .fieldSeparator(".")
            .loadModule(storeModule);

        db = nitriteBuilder.openOrCreate();

        ObjectRepository<TestData> repository = db.getRepository(TestData.class);
        for (int i = 0; i < 10; i++) {
            TestData testData = new TestData();
            testData.setId(UUID.randomUUID().toString());
            testData.setLocalDateTime(LocalDateTime.now());
            testData.setDuration(Duration.ofDays(i));
            repository.insert(testData);
        }

        assertEquals(10, repository.find().size());
        for (TestData testData : repository.find()) {
            assertNotNull(testData.localDateTime);
        }
    }

    @Data
    private static class TestData {
        private String id;
        private LocalDateTime localDateTime;
        private Duration duration;
    }
}
