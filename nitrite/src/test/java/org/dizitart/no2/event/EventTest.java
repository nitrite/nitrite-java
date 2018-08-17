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

package org.dizitart.no2.event;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.data.Employee;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.filters.ObjectFilters.ALL;
import static org.dizitart.no2.filters.ObjectFilters.eq;
import static org.dizitart.no2.util.Iterables.firstOrDefault;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Anindya Chatterjee.
 */
@RunWith(Parameterized.class)
public class EventTest {
    private String fileName = getRandomTempDbFile();
    private Nitrite db;
    private ObjectRepository<Employee> employeeRepository;
    private SampleListener listener;

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
        return Arrays.asList(new Object[][]{
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

        if (!isProtected) {
            db = builder.openOrCreate("test-user", "test-password");
        } else {
            db = builder.openOrCreate();
        }

        employeeRepository = db.getRepository(Employee.class);
        listener = new SampleListener();
        employeeRepository.register(listener);
    }

    @Test
    public void testInsert() {
        Employee employee = new Employee();
        employee.setEmpId(1L);
        employeeRepository.insert(employee);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.INSERT));
        assertEquals(listener.getAction(), ChangeType.INSERT);
        assertEquals(listener.getItems().size(), 1);
    }

    @Test
    public void testUpdate() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");
        employeeRepository.insert(e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.INSERT));
        assertEquals(listener.getAction(), ChangeType.INSERT);
        assertEquals(listener.getItems().size(), 1);

        e.setAddress("xyz");
        employeeRepository.update(eq("empId", 1L), e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.UPDATE));
        assertEquals(listener.getAction(), ChangeType.UPDATE);
        assertEquals(listener.getItems().size(), 1);

        ChangedItem item = firstOrDefault(listener.getItems());
        Employee byId = employeeRepository.getById(item.getDocument().getId());
        assertEquals(byId.getAddress(), "xyz");
    }

    @Test
    public void testUpsert() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.update(eq("empId", 1), e, true);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.INSERT));
        assertEquals(listener.getAction(), ChangeType.INSERT);
        assertEquals(listener.getItems().size(), 1);
    }

    @Test
    public void testDelete() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.insert(e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.INSERT));

        employeeRepository.remove(eq("empId", 1L));
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.REMOVE));

        System.out.println("Action - " + listener.getAction());
        assertEquals(listener.getAction(), ChangeType.REMOVE);
        assertEquals(listener.getItems().size(), 1);
    }

    @Test
    public void testDrop() {
        employeeRepository.drop();
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.DROP));
        assertEquals(listener.getAction(), ChangeType.DROP);
        assertNull(listener.getItems());
    }

    @Test
    public void testClose() {
        if (!employeeRepository.isClosed()) {
            employeeRepository.close();
        }

        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.CLOSE));
        assertEquals(listener.getAction(), ChangeType.CLOSE);
        assertNull(listener.getItems());
    }

    @Test
    public void testDeregister() {
        employeeRepository.deregister(listener);
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.insert(e);
        assertNull(listener.getAction());
        assertNull(listener.getItems());
    }

    @Test
    public void testMultipleListeners() {
        final AtomicInteger count = new AtomicInteger(0);
        employeeRepository.register(new ChangeListener() {
            @Override
            public void onChange(ChangeInfo changeInfo) {
                count.incrementAndGet();
            }
        });

        employeeRepository.register(new ChangeListener() {
            @Override
            public void onChange(ChangeInfo changeInfo) {
                count.incrementAndGet();
            }
        });

        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");
        employeeRepository.insert(e);

        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(ChangeType.INSERT));
        assertEquals(count.get(), 2);
    }

    @After
    public void clear() throws IOException {
        if (employeeRepository != null) {
            if (!employeeRepository.isDropped()
                    && !employeeRepository.isClosed()) {
                employeeRepository.remove(ALL);
                employeeRepository.deregister(listener);
                employeeRepository.close();
            }
        }

        if (db != null) {
            db.commit();
            db.close();
        }

        if (!inMemory) {
            Files.delete(Paths.get(fileName));
        }
    }

    private Callable<Boolean> listenerPrepared(final ChangeType action) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return listener.getAction() == action;
            }
        };
    }
}
