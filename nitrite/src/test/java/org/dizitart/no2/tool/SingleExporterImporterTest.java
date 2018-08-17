/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.tool;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.PersistentCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.data.Company;
import org.dizitart.no2.collection.objects.data.DataGenerator;
import org.dizitart.no2.collection.objects.data.Employee;
import org.dizitart.no2.index.Index;
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
