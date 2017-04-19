package org.dizitart.no2.benchmark.tests;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.benchmark.core.Benchmark;
import org.dizitart.no2.benchmark.data.Person;
import org.dizitart.no2.objects.ObjectRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.dizitart.no2.objects.filters.ObjectFilters.ALL;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseNitriteBenchmark implements Benchmark {
    private String fileName = getDbFile();
    private BenchmarkTestHelper testHelper = new BenchmarkTestHelper();
    Nitrite db;
    Person[] personList;
    ObjectRepository<Person> personRepository;

    @Override
    public void beforeTest() {
        NitriteBuilder builder = Nitrite.builder();
        builder.filePath(fileName);
        builder.compressed();
        db = builder.openOrCreate();
        personList = testHelper.loadData();
        personRepository = db.getRepository(Person.class);
    }

    @Override
    public void afterTest() throws IOException {
        if (personRepository != null && !personRepository.isDropped()) {
            personRepository.remove(ALL);
        }

        closeDb();
        Files.delete(Paths.get(fileName));
    }

    @Override
    public void afterRun() {
        if (personRepository != null && !personRepository.isDropped()) {
            personRepository.remove(ALL);
        }
        db.compact();
    }

    public void closeDb() {
        if (db != null) {
            db.commit();
            db.close();
        }
    }

    public static String getDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }
}
