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
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.data.Company;
import org.dizitart.no2.collection.objects.data.Employee;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseExternalTest {
    protected ObjectRepository<Employee> sourceEmpRepo;
    protected ObjectRepository<Company> sourceCompRepo;
    protected NitriteCollection sourceFirstColl;
    protected NitriteCollection sourceSecondColl;
    protected Nitrite sourceDb;
    protected Nitrite destDb;
    protected String schemaFile;
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

    protected List<Document> filter(List<Document> documents) {
        for(Document document : documents) {
            document.remove(DOC_REVISION);
            document.remove(DOC_MODIFIED);
            document.remove(DOC_SOURCE);
            document.remove(DOC_SYNCED);
        }
        return documents;
    }
}
