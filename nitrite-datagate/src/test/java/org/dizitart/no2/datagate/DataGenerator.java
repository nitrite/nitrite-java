package org.dizitart.no2.datagate;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class DataGenerator {
    private static Random random = new Random(System.currentTimeMillis());
    private static AtomicInteger counter = new AtomicInteger(random.nextInt());

    public static Employee generateEmployee(int depth) {
        Employee employee = new Employee();
        employee.setEmpId(System.nanoTime() + counter.incrementAndGet());
        employee.setJoinDate(randomDate());
        employee.setAddress(UUID.randomUUID().toString().replace('-', ' '));

        byte[] blob = new byte[random.nextInt(8000)];
        random.nextBytes(blob);
        employee.setBlob(blob);
        if (depth == 0) {
            Employee emp1 = generateEmployee(1);
            Employee emp2 = generateEmployee(1);
            employee.setSubordinates(Arrays.asList(emp1, emp2));
        }

        return employee;
    }

    private static Date randomDate() {
        return new Date(-946771200000L +
                (Math.abs(random.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000)));
    }
}
