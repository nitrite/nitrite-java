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

package org.dizitart.no2.collection.objects.data;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class DataGenerator {
    private static Random random = new Random(System.currentTimeMillis());
    private static AtomicInteger counter = new AtomicInteger(random.nextInt());

    public static Company generateCompanyRecord() {
        Company company = new Company();
        company.setCompanyId(System.nanoTime() + counter.incrementAndGet());
        company.setCompanyName(randomCompanyName());
        company.setDateCreated(randomDate());
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
        employee.setJoinDate(randomDate());
        employee.setAddress(UUID.randomUUID().toString().replace('-', ' '));

        byte[] blob = new byte[random.nextInt(8000)];
        random.nextBytes(blob);
        employee.setBlob(blob);
        employee.setEmployeeNote(randomNote());

        return employee;
    }

    private static Date randomDate() {
        return new Date(-946771200000L +
                (Math.abs(random.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000)));
    }

    private static Note randomNote() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("test.text");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String strLine;
        long line = random.nextInt(49);
        int count = 0;
        try {
            while ((strLine = br.readLine()) != null) {
                if (count == line) {
                    Note note = new Note();
                    note.setNoteId(line);
                    note.setText(strLine);
                    return note;
                }
                count++;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return null;
    }

    private static String randomCompanyName() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("english.stop");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String strLine;
        int line = counter.incrementAndGet();
        int count = 0;
        try {
            while ((strLine = br.readLine()) != null) {
                if (count == line) {
                    return strLine + " inc.";
                }
                count++;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return null;
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
