package org.dizitart.no2.tool;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.data.Company;
import org.dizitart.no2.objects.data.Employee;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.dizitart.no2.Constants.*;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseExternalTest {
    ObjectRepository<Employee> sourceEmpRepo;
    ObjectRepository<Company> sourceCompRepo;
    NitriteCollection sourceFirstColl;
    NitriteCollection sourceSecondColl;
    Nitrite sourceDb;
    Nitrite destDb;
    String schemaFile;
    private String sourceDbFile;
    private String destDbFile;

    @Before
    public void setUp() {
        sourceDbFile = getRandomTempDbFile();
        destDbFile = getRandomTempDbFile();

        sourceDb = Nitrite.builder()
                .filePath(sourceDbFile)
                .openOrCreate();

        destDb = Nitrite.builder()
                .filePath(destDbFile)
                .openOrCreate();

        sourceEmpRepo = sourceDb.getRepository(Employee.class);
        sourceCompRepo = sourceDb.getRepository(Company.class);

        sourceFirstColl = sourceDb.getCollection("first");
        sourceSecondColl = sourceDb.getCollection("second");
    }

    @After
    public void cleanUp() throws IOException {
        sourceFirstColl.close();
        sourceSecondColl.close();
        sourceEmpRepo.close();
        sourceCompRepo.close();

        sourceDb.close();
        destDb.close();

        Files.delete(Paths.get(sourceDbFile));
        Files.delete(Paths.get(destDbFile));
        Files.delete(Paths.get(schemaFile));
    }

    List<Document> filter(List<Document> documents) {
        for(Document document : documents) {
            document.remove(DOC_REVISION);
            document.remove(DOC_MODIFIED);
            document.remove(DOC_SOURCE);
            document.remove(DOC_SYNCED);
        }
        return documents;
    }
}
