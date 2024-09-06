package org.dizitart.no2.mapper.jackson.integration.repository;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.dizitart.no2.mapper.jackson.integration.repository.BaseObjectRepositoryTest.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test(expected = ObjectMappingException.class)
    public void testJavaTime() {
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

        assertEquals(repository.find().size(), 10);
        for (TestData testData : repository.find()) {
            assertNotNull(testData.localDateTime);
        }
    }

    @Test
    public void testJavaTimeModule() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(fileName)
            .build();

        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .loadModule(new JacksonMapperModule(new JavaTimeModule()))
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

        assertEquals(repository.find().size(), 10);
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
