/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.support;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.PersistentCollection;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ExporterImporterOptionTest extends BaseExternalTest {

    @Test
    public void testImportExportSingle() {
        schemaFile = System.getProperty("java.io.tmpdir") + File.separator
            + "nitrite" + File.separator + "single-schema.json";

        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sourceEmpRepo.insert(DataGenerator.generateEmployee());
            sourceKeyedEmpRepo.insert(DataGenerator.generateEmployee());

            Document document = createDocument("first-field", random.nextGaussian());
            sourceFirstColl.insert(document);
        }

        Exporter exporter = Exporter.of(sourceDb);
        exporter.withOptions(new ExportOptions() {{
            setCollections(new ArrayList<PersistentCollection<?>>() {{
                add(sourceEmpRepo);
                add(sourceKeyedEmpRepo);
                add(sourceFirstColl);
            }});
        }});
        exporter.exportTo(schemaFile);

        Importer importer = Importer.of(destDb);
        importer.importFrom(schemaFile);

        ObjectRepository<Employee> destEmpRepo = destDb.getRepository(Employee.class);
        ObjectRepository<Employee> destKeyedEmpRepo = destDb.getRepository(Employee.class, "key");
        NitriteCollection destFirstColl = destDb.getCollection("first");

        assertEquals(filter(sourceFirstColl.find().toList()),
            filter(destFirstColl.find().toList()));
        assertEquals(sourceEmpRepo.find().toList(),
            destEmpRepo.find().toList());
        assertEquals(sourceKeyedEmpRepo.find().toList(),
            destKeyedEmpRepo.find().toList());

        assertEquals(sourceEmpRepo.listIndices(), destEmpRepo.listIndices());
        assertEquals(sourceKeyedEmpRepo.listIndices(), destKeyedEmpRepo.listIndices());
        assertEquals(sourceFirstColl.listIndices(), destFirstColl.listIndices());

        ObjectRepository<Company> destCompRepo = destDb.getRepository(Company.class);
        NitriteCollection destSecondColl = destDb.getCollection("second");

        assertEquals(filter(destSecondColl.find().toList()),
            new ArrayList<Document>());
        assertEquals(destCompRepo.find().toList(),
            new ArrayList<Company>());

        assertEquals(destCompRepo.listIndices(), sourceCompRepo.listIndices());
        assertEquals(destSecondColl.listIndices(), new LinkedHashSet<IndexEntry>());
    }
}
