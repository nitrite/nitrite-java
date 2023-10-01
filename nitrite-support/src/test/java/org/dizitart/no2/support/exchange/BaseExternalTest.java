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

 package org.dizitart.no2.support.exchange;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.support.Retry;
import org.dizitart.no2.support.TestUtil;
import org.dizitart.no2.support.data.Company;
import org.dizitart.no2.support.data.Employee;
import org.dizitart.no2.support.data.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.module.NitriteModule.module;
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
     protected String sourceDbFile;
     protected String destDbFile;
 
     @Rule
     public Retry retry = new Retry(3);
 
     @Before
     public void setUp() {
         sourceDbFile = getRandomTempDbFile();
         destDbFile = getRandomTempDbFile();
         openDb();
     }
 
     @After
     public void cleanUp() throws IOException {
         closeDb();
         TestUtil.deleteDb(sourceDbFile);
         TestUtil.deleteDb(destDbFile);
         TestUtil.deleteDb(schemaFile);
     }
 
     public static String getRandomTempDbFile() {
         String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
         File file = new File(dataDir);
         if (!file.exists()) {
             assertTrue(file.mkdirs());
         }
         return file.getPath() + File.separator + UUID.randomUUID() + ".db";
     }
 
     protected void openDb() {
         sourceDb = createDb(sourceDbFile);
         destDb = createDb(destDbFile);
 
         sourceEmpRepo = sourceDb.getRepository(Employee.class);
         sourceKeyedEmpRepo = sourceDb.getRepository(Employee.class, "key");
         sourceCompRepo = sourceDb.getRepository(Company.class);
 
         sourceFirstColl = sourceDb.getCollection("first");
         sourceSecondColl = sourceDb.getCollection("second");
     }
 
     protected void closeDb() {
         sourceFirstColl.close();
         sourceSecondColl.close();
         sourceEmpRepo.close();
         sourceCompRepo.close();
 
         sourceDb.close();
         destDb.close();
     }
 
     protected List<Document> filter(List<Document> documents) {
         for (Document document : documents) {
             document.remove(DOC_REVISION);
             document.remove(DOC_MODIFIED);
             document.remove(DOC_SOURCE);
         }
         return documents;
     }
 
     protected Nitrite createDb(String filePath) {
         MVStoreModule storeModule = MVStoreModule.withConfig()
             .filePath(filePath)
             .build();
 
         SimpleNitriteMapper documentMapper = new SimpleNitriteMapper();
         documentMapper.registerEntityConverter(new Employee.EmployeeConverter());
         documentMapper.registerEntityConverter(new Company.CompanyConverter());
         documentMapper.registerEntityConverter(new Note.NoteConverter());
 
         return Nitrite.builder()
             .loadModule(storeModule)
             .loadModule(module(documentMapper))
             .fieldSeparator(".")
             .openOrCreate();
     }
 }
 