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

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.objects.data.*;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.filters.ObjectFilters.ALL;

@RunWith(value = Parameterized.class)
public abstract class BaseObjectRepositoryTest {
    private String fileName = getRandomTempDbFile();
    protected Nitrite db;
    ObjectRepository<Company> companyRepository;
    ObjectRepository<Employee> employeeRepository;

    ObjectRepository<ClassA> aObjectRepository;
    ObjectRepository<ClassC> cObjectRepository;

    @Parameterized.Parameter
    public boolean inMemory = false;

    @Parameterized.Parameter(value = 1)
    public boolean isProtected = false;

    @Parameterized.Parameter(value = 2)
    public boolean isCompressed = false;

    @Parameterized.Parameter(value = 3)
    public boolean isAutoCommit = false;

    @Parameterized.Parameter(value = 4)
    public boolean isAutoCompact = false;

    @Parameterized.Parameters(name = "InMemory = {0}, Protected = {1}, " +
            "Compressed = {2}, AutoCommit = {3}, AutoCompact = {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {false, false, false, false, false},
                {false, false, false, true, false},
                {false, false, true, false, false},
                {false, false, true, true, false},
                {false, true, false, false, false},
                {false, true, false, true, false},
                {false, true, true, false, false},
                {false, true, true, true, true},
                {true, false, false, false, true},
                {true, false, false, true, true},
                {true, false, true, false, true},
                {true, false, true, true, true},
                {true, true, false, false, true},
                {true, true, false, true, true},
                {true, true, true, false, true},
                {true, true, true, true, true},
        });
    }

    @Before
    public void setUp() {
        openDb();

        companyRepository = db.getRepository(Company.class);
        employeeRepository = db.getRepository(Employee.class);

        aObjectRepository = db.getRepository(ClassA.class);
        cObjectRepository = db.getRepository(ClassC.class);

        for (int i = 0; i < 10; i++) {
            Company company = DataGenerator.generateCompanyRecord();
            companyRepository.insert(company);
            Employee employee = DataGenerator.generateEmployee();
            employee.setEmpId((long) i + 1);
            employeeRepository.insert(employee);

            aObjectRepository.insert(ClassA.create(i + 50));
            cObjectRepository.insert(ClassC.create(i + 30));
        }
    }

    private void openDb() {
        NitriteBuilder builder = Nitrite.builder();

        if (!isAutoCommit) {
            builder.disableAutoCommit();
        }

        if (!inMemory) {
            builder.filePath(fileName);
        }

        if (isCompressed) {
            builder.compressed();
        }

        if (!isAutoCompact) {
            builder.disableAutoCompact();
        }

        if (isProtected) {
            db = builder.openOrCreate("test-user", "test-password");
        } else {
            db = builder.openOrCreate();
        }
    }

    @After
    public void clear() throws IOException {
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

        if (db != null) {
            db.commit();
            db.close();
        }

        if (!inMemory) {
            Files.delete(Paths.get(fileName));
        }
    }
}
