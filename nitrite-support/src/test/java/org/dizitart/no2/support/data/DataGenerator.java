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

package org.dizitart.no2.support.data;

import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class DataGenerator {
    private static Random random = new Random(System.currentTimeMillis());
    private static AtomicInteger counter = new AtomicInteger(random.nextInt());
    private static Faker faker = new Faker();

    public static Company generateCompanyRecord() {
        Company company = new Company();
        company.setCompanyId(System.nanoTime() + counter.incrementAndGet());
        company.setCompanyName(faker.company().name());
        company.setDateCreated(faker.date().past(20 * 365, TimeUnit.DAYS));
        List<String> departments = departments();
        company.setDepartments(departments);

        Map<String, List<Employee>> employeeRecord = new HashMap<>();
        for (String department : departments) {
            employeeRecord.put(department,
                generateEmployeeRecords(company, random.nextInt(20)));
        }
        company.setEmployeeRecord(employeeRecord);
        return company;
    }

    private static List<Employee> generateEmployeeRecords(Company company, int count) {
        List<Employee> employeeList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Employee employee = generateEmployee();
            employee.setCompany(company);
            employeeList.add(employee);
        }
        return employeeList;
    }

    public static Employee generateEmployee() {
        Employee employee = new Employee();
        employee.setEmpId(System.nanoTime() + counter.incrementAndGet());
        employee.setJoinDate(faker.date().birthday());
        employee.setAddress(faker.address().fullAddress());

        byte[] blob = new byte[random.nextInt(8000)];
        random.nextBytes(blob);
        employee.setBlob(blob);
        employee.setEmployeeNote(randomNote());

        return employee;
    }

    public static Note randomNote() {
        Note note = new Note();
        note.setNoteId((long) counter.incrementAndGet());
        note.setText(faker.lorem().paragraph());
        return note;
    }

    private static List<String> departments() {
        List<String> departments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            departments.add(faker.job().title());
        }
        return departments;
    }
}
