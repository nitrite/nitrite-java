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
