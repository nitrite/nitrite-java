package org.dizitart.no2.tool;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.data.Company;
import org.dizitart.no2.objects.data.DataGenerator;
import org.dizitart.no2.objects.data.Employee;
import org.junit.Test;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.dizitart.no2.Document.createDocument;

/**
 * @author Anindya Chatterjee.
 */
public class ExporterImporterTest extends BaseExternalTest {

    @Test
    public void testImportExport() {
        schemaFile = System.getProperty("java.io.tmpdir") + File.separator
                + "nitrite" + File.separator + "schema.json";

        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sourceEmpRepo.insert(DataGenerator.generateEmployee());
            sourceCompRepo.insert(DataGenerator.generateCompanyRecord());

            Document document = createDocument("first-field", random.nextGaussian());
            sourceFirstColl.insert(document);

            document = createDocument("second-field", random.nextLong());
            sourceSecondColl.insert(document);
        }

        Exporter exporter = Exporter.of(sourceDb);
        exporter.exportTo(schemaFile);

        Importer importer = Importer.of(destDb);
        importer.importFrom(schemaFile);

        NitriteCollection destFirstColl = destDb.getCollection("first");
        NitriteCollection destSecondColl = destDb.getCollection("second");
        ObjectRepository<Employee> destEmpRepo = destDb.getRepository(Employee.class);
        ObjectRepository<Company> destCompRepo = destDb.getRepository(Company.class);

        assertEquals(filter(sourceFirstColl.find().toList()),
                filter(destFirstColl.find().toList()));
        assertEquals(filter(sourceSecondColl.find().toList()),
                filter(destSecondColl.find().toList()));

        assertEquals(sourceEmpRepo.find().toList(),
                destEmpRepo.find().toList());
        assertEquals(sourceCompRepo.find().toList(),
                destCompRepo.find().toList());

        assertEquals(sourceEmpRepo.listIndices(), destEmpRepo.listIndices());
        assertEquals(sourceCompRepo.listIndices(), destCompRepo.listIndices());
        assertEquals(sourceFirstColl.listIndices(), destFirstColl.listIndices());
        assertEquals(sourceSecondColl.listIndices(), destSecondColl.listIndices());
    }
}
