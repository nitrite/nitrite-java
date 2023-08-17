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

package org.dizitart.no2.integration.event;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.mapper.EntityConverterMapper;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.repository.data.Employee;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@RunWith(Parameterized.class)
public class EventTest {
    @Parameterized.Parameter
    public boolean isProtected = false;

    private final String fileName = getRandomTempDbFile();

    private Nitrite db;
    private ObjectRepository<Employee> employeeRepository;
    private SampleListenerCollection listener;

	@Rule
    public Retry retry = new Retry(3);
    @Parameterized.Parameters(name = "Protected = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true},
        });
    }

    @Before
    public void setUp() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
                .filePath(fileName)
                .build();

        if (isProtected) {
            db = Nitrite.builder()
                    .fieldSeparator(".")
                    .loadModule(storeModule)
                    .openOrCreate("test-user", "test-password");
        } else {
            db = Nitrite.builder()
                    .fieldSeparator(".")
                    .loadModule(storeModule)
                    .openOrCreate();
        }

        EntityConverterMapper documentMapper = (EntityConverterMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new Employee.EmployeeConverter());

        employeeRepository = db.getRepository(Employee.class);
        listener = new SampleListenerCollection();
        employeeRepository.subscribe(listener);
    }

    @Test
    public void testInsert() {
        Employee employee = new Employee();
        employee.setEmpId(1L);
        employeeRepository.insert(employee);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));
        assertEquals(listener.getAction(), EventType.Insert);
        assertNotNull(listener.getItem());
    }

    @Test
    public void testUpdate() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");
        employeeRepository.insert(e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));
        assertEquals(listener.getAction(), EventType.Insert);
        assertNotNull(listener.getItem());

        e.setAddress("xyz");
        employeeRepository.update(where("empId").eq(1L), e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Update));
        assertEquals(listener.getAction(), EventType.Update);
        assertNotNull(listener.getItem());

        Employee byId = employeeRepository.getById(1L);
        assertEquals(byId.getAddress(), "xyz");
    }

    @Test
    public void testUpsert() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.update(where("empId").eq(1), e, updateOptions(true));
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));
        assertEquals(listener.getAction(), EventType.Insert);
        assertNotNull(listener.getItem());
    }

    @Test
    public void testDelete() {
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.insert(e);
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));

        employeeRepository.remove(where("empId").eq(1L));
        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Remove));

        assertEquals(listener.getAction(), EventType.Remove);
        assertNotNull(listener.getItem());
    }

    @Test
    public void testDrop() {
        employeeRepository.drop();
        assertNull(listener.getItem());
    }

    @Test
    public void testClose() {
        if (employeeRepository.isOpen()) {
            employeeRepository.close();
        }
        assertNull(listener.getItem());
    }

    @Test
    public void testDeregister() {
        employeeRepository.unsubscribe(listener);
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");

        employeeRepository.insert(e);
        assertNull(listener.getAction());
        assertNull(listener.getItem());
    }

    @Test
    public void testMultipleListeners() {
        final AtomicInteger count = new AtomicInteger(0);
        employeeRepository.subscribe(changeInfo -> count.incrementAndGet());

        employeeRepository.subscribe(changeInfo -> count.incrementAndGet());

        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");
        employeeRepository.insert(e);

        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));
        assertEquals(count.get(), 2);
    }

    @Test
    public void testSingleEventListener() {
        final AtomicInteger count = new AtomicInteger(0);
        employeeRepository.subscribe(changeInfo -> count.incrementAndGet());

        employeeRepository = db.getRepository(Employee.class);
        Employee e = new Employee();
        e.setEmpId(1L);
        e.setAddress("abcd");
        employeeRepository.insert(e);

        await().atMost(1, TimeUnit.SECONDS).until(listenerPrepared(EventType.Insert));
        assertEquals(count.get(), 1);
    }

    @After
    public void clear() throws IOException {
        if (employeeRepository != null) {
            if (!employeeRepository.isDropped()
                    && employeeRepository.isOpen()) {
                employeeRepository.remove(ALL);
                employeeRepository.unsubscribe(listener);
                employeeRepository.close();
            }
        }

        if (db != null) {
            db.commit();
            db.close();
        }

        deleteDb(fileName);
    }

    private Callable<Boolean> listenerPrepared(final EventType action) {
        return () -> listener.getAction() == action;
    }
}
