package org.dizitart.no2.ui.data;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

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

    public static List<Employee> generateEmployeeRecords(Company company, int count) {
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
        employee.setEmpId(Math.abs(random.nextLong()));
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
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("note.text");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String strLine;
        int line = random.nextInt(49);
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
        return RandomStringUtils.randomAlphabetic(10);
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
