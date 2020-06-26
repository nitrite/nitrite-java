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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.dizitart.no2.common.Constants.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseExternalTest {
    protected ObjectRepository<Employee> sourceEmpRepo;
    protected ObjectRepository<Employee> sourceKeyedEmpRepo;
    protected ObjectRepository<Company> sourceCompRepo;
    protected NitriteCollection sourceFirstColl;
    protected NitriteCollection sourceSecondColl;
    protected Nitrite sourceDb;
    protected Nitrite destDb;
    protected String schemaFile;
    private String sourceDbFile;
    private String destDbFile;

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }

    @Before
    public void setUp() {
        sourceDbFile = getRandomTempDbFile();
        destDbFile = getRandomTempDbFile();

        sourceDb = NitriteBuilder.get()
            .filePath(sourceDbFile)
            .openOrCreate();

        destDb = NitriteBuilder.get()
            .filePath(destDbFile)
            .openOrCreate();

        sourceEmpRepo = sourceDb.getRepository(Employee.class);
        sourceKeyedEmpRepo = sourceDb.getRepository(Employee.class, "key");
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
        for (Document document : documents) {
            document.remove(DOC_REVISION);
            document.remove(DOC_MODIFIED);
            document.remove(DOC_SOURCE);
        }
        return documents;
    }
}
