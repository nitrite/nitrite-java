/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.repository.data.*;
import org.dizitart.no2.integration.repository.decorator.ManufacturerConverter;
import org.dizitart.no2.integration.repository.decorator.MiniProduct;
import org.dizitart.no2.integration.repository.decorator.ProductConverter;
import org.dizitart.no2.integration.repository.decorator.ProductIdConverter;
import org.dizitart.no2.integration.transaction.TxData;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.mvstore.MVStoreModuleBuilder;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;

@RunWith(value = Parameterized.class)
public abstract class BaseObjectRepositoryTest {
    @Parameterized.Parameter
    public boolean inMemory = false;
    @Parameterized.Parameter(value = 1)
    public boolean isProtected = false;
    @Parameterized.Parameter(value = 2)
    public boolean isCompressed = false;
    @Parameterized.Parameter(value = 3)
    public boolean isAutoCommit = false;

    protected Nitrite db;
    protected ObjectRepository<Company> companyRepository;
    protected ObjectRepository<Employee> employeeRepository;
    protected ObjectRepository<ClassA> aObjectRepository;
    protected ObjectRepository<ClassC> cObjectRepository;
	protected ObjectRepository<Book> bookRepository;
	
    private final String fileName = getRandomTempDbFile();

    @Rule
    public Retry retry = new Retry(3);

    @Parameterized.Parameters(name = "InMemory = {0}, Protected = {1}, " +
        "Compressed = {2}, AutoCommit = {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false, false, false, false},
            {false, false, false, true},
            {false, false, true, false},
            {false, false, true, true},
            {false, true, false, false},
            {false, true, false, true},
            {false, true, true, false},
            {false, true, true, true},
            {true, false, false, false},
            {true, false, false, true},
            {true, false, true, false},
            {true, false, true, true},
            {true, true, false, false},
            {true, true, false, true},
            {true, true, true, false},
            {true, true, true, true},
        });
    }

    @Before
    public void setUp() {
        openDb();

        companyRepository = db.getRepository(Company.class);
        employeeRepository = db.getRepository(Employee.class);

        aObjectRepository = db.getRepository(ClassA.class);
        cObjectRepository = db.getRepository(ClassC.class);

        bookRepository = db.getRepository(Book.class);

        for (int i = 0; i < 10; i++) {
            Company company = DataGenerator.generateCompanyRecord();
            companyRepository.insert(company);
            Employee employee = DataGenerator.generateEmployee();
            employee.setEmpId((long) i + 1);
            employeeRepository.insert(employee);

            aObjectRepository.insert(ClassA.create(i + 50));
            cObjectRepository.insert(ClassC.create(i + 30));

            Book book = DataGenerator.randomBook();
            bookRepository.insert(book);
        }
    }

    protected void openDb() {
        MVStoreModuleBuilder builder = MVStoreModule.withConfig();

        if (isCompressed) {
            builder.compress(true);
        }

        if (!isAutoCommit) {
            builder.autoCommit(false);
        }

        if (!inMemory) {
            builder.filePath(fileName);
        }

        MVStoreModule storeModule = builder.build();
        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule);

        if (isProtected) {
            db = nitriteBuilder.openOrCreate("test-user", "test-password");
        } else {
            db = nitriteBuilder.openOrCreate();
        }

        SimpleDocumentMapper documentMapper = (SimpleDocumentMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new RepositoryJoinTest.Person.Converter());
        documentMapper.registerEntityConverter(new RepositoryJoinTest.Address.Converter());
        documentMapper.registerEntityConverter(new RepositoryJoinTest.PersonDetails.Converter());
        documentMapper.registerEntityConverter(new Company.CompanyConverter());
        documentMapper.registerEntityConverter(new Employee.EmployeeConverter());
        documentMapper.registerEntityConverter(new Note.NoteConverter());
        documentMapper.registerEntityConverter(new Book.BookConverter());
        documentMapper.registerEntityConverter(new BookId.BookIdConverter());
        documentMapper.registerEntityConverter(new ClassA.ClassAConverter());
        documentMapper.registerEntityConverter(new ClassBConverter());
        documentMapper.registerEntityConverter(new ClassC.ClassCConverter());
        documentMapper.registerEntityConverter(new ElemMatch.Converter());
        documentMapper.registerEntityConverter(new InternalClass.Converter());
        documentMapper.registerEntityConverter(new UniversalTextTokenizerTest.TextData.Converter());
        documentMapper.registerEntityConverter(new SubEmployee.Converter());
        documentMapper.registerEntityConverter(new ProductScore.Converter());
        documentMapper.registerEntityConverter(new PersonEntity.Converter());
        documentMapper.registerEntityConverter(new RepeatableIndexTest.Converter());
        documentMapper.registerEntityConverter(new EncryptedPerson.Converter());
        documentMapper.registerEntityConverter(new TxData.Converter());
        documentMapper.registerEntityConverter(new WithNitriteId.WithNitriteIdConverter());
        documentMapper.registerEntityConverter(new ProductConverter());
        documentMapper.registerEntityConverter(new ProductIdConverter());
        documentMapper.registerEntityConverter(new ManufacturerConverter());
        documentMapper.registerEntityConverter(new MiniProduct.Converter());
    }

    @After
    public void clear() throws Exception {
        if (companyRepository != null && !companyRepository.isDropped()) {
            companyRepository.remove(ALL);
        }

        if (employeeRepository != null && !employeeRepository.isDropped()) {
            employeeRepository.remove(ALL);
        }

        if (aObjectRepository != null && !aObjectRepository.isDropped()) {
            aObjectRepository.remove(ALL);
        }

        if (cObjectRepository != null && !cObjectRepository.isDropped()) {
            cObjectRepository.remove(ALL);
        }

        if (bookRepository != null && !bookRepository.isDropped()) {
            bookRepository.remove(ALL);
        }

        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }

        if (!inMemory) {
            deleteDb(fileName);
        }
    }
}
