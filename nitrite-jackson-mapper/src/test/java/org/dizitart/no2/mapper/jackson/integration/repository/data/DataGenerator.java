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

package org.dizitart.no2.mapper.jackson.integration.repository.data;

import com.github.javafaker.Faker;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anindya Chatterjee.
 */
public class DataGenerator {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final AtomicInteger counter = new AtomicInteger(random.nextInt());
    private static final Faker faker = new Faker(random);

    private DataGenerator() {}

    public static Company generateCompanyRecord() {
        Company company = new Company();
        company.setCompanyId(System.nanoTime() + counter.incrementAndGet());
        company.setCompanyName(faker.company().name());
        company.setDateCreated(faker.date().past(10, TimeUnit.DAYS));
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

        employee.setBlob(faker.lorem().paragraph().getBytes(StandardCharsets.UTF_8));
        employee.setEmployeeNote(randomNote());
        employee.setEmailAddress(faker.internet().emailAddress());

        return employee;
    }

    public static Note randomNote() {
        Note note = new Note();
        note.setNoteId(System.nanoTime() + counter.incrementAndGet());
        note.setText(faker.lorem().paragraph());
        return note;
    }

    public static Book randomBook() {
        BookId bookId = new BookId();
        val bookFaker = faker.book();
        bookId.setIsbn(faker.idNumber().ssnValid());
        bookId.setAuthor(bookFaker.author());
        bookId.setName(bookFaker.title());

        Book book = new Book();
        book.setBookId(bookId);
        book.setDescription(faker.backToTheFuture().quote());
        book.setPrice(faker.number().randomDouble(2, 100, 500));
        book.setPublisher(bookFaker.publisher());
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tags.add(bookFaker.genre());
        }
        book.setTags(tags);
        return book;
    }

    private static List<String> departments() {
        return new ArrayList<String>() {{
            add("dev");
            add("hr");
            add("qa");
            add("dev-ops");
            add("sales");
            add("marketing");
            add("design");
            add("support");
        }};
    }
}
