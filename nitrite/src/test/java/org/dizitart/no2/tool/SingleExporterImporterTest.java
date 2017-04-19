package org.dizitart.no2.tool;

import org.dizitart.no2.Document;
import org.dizitart.no2.Index;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.PersistentCollection;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.data.Company;
import org.dizitart.no2.objects.data.DataGenerator;
import org.dizitart.no2.objects.data.Employee;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class SingleExporterImporterTest extends BaseExternalTest {

    @Test
    public void testImportExportSingle() {
        schemaFile = System.getProperty("java.io.tmpdir") + File.separator
                + "nitrite" + File.separator + "single-schema.json";

        for (int i = 0; i < 5; i++) {
            sourceEmpRepo.insert(DataGenerator.generateEmployee());
        }

        Exporter exporter = Exporter.of(sourceDb);
        exporter.withOptions(new ExportOptions() {{
            setCollections(new ArrayList<PersistentCollection<?>>() {{
                add(sourceEmpRepo);
            }});
        }});
        exporter.exportTo(schemaFile);

        Importer importer = Importer.of(destDb);
        importer.importFrom(schemaFile);

        ObjectRepository<Employee> destEmpRepo = destDb.getRepository(Employee.class);
        assertEquals(sourceEmpRepo.find().toList(),
                destEmpRepo.find().toList());
        assertEquals(sourceEmpRepo.listIndices(), destEmpRepo.listIndices());

        ObjectRepository<Company> destCompRepo = destDb.getRepository(Company.class);
        NitriteCollection destFirstColl = destDb.getCollection("first");
        NitriteCollection destSecondColl = destDb.getCollection("second");

        assertEquals(filter(destFirstColl.find().toList()),
                new ArrayList<Document>());
        assertEquals(filter(destSecondColl.find().toList()),
                new ArrayList<Document>());
        assertEquals(destCompRepo.find().toList(),
                new ArrayList<Company>());

        assertEquals(destCompRepo.listIndices(), sourceCompRepo.listIndices());
        assertEquals(destFirstColl.listIndices(), new LinkedHashSet<Index>());
        assertEquals(destSecondColl.listIndices(), new LinkedHashSet<Index>());
    }
}
