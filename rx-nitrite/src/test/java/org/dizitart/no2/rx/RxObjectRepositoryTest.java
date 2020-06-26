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

package org.dizitart.no2.rx;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.TestSubscriber;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.filters.Filter;
import org.junit.Before;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class RxObjectRepositoryTest extends RxBaseTest {
    private RxObjectRepository<Employee> repository;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        repository = db.getRepository(Employee.class);
    }

    @Test
    public void testInsert() {
        Flowable<NitriteId> flowable = repository.insert(testData.toArray(new Employee[0]));
        assertEquals(0, repository.find().size().blockingGet().intValue());

        Disposable subscribe = repository.observe().subscribe(collectionEventInfo -> {
            System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
        });

        assertEquals(0, repository.find().size().blockingGet().intValue());
        TestSubscriber<NitriteId> test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(2, repository.find().size().blockingGet().intValue());

        subscribe.dispose();
    }

    @Test
    public void testUpdate() {
        Employee e1 = new Employee("John Doe", 35);
        Employee e2 = new Employee("Jane Doe", 30);
        Flowable<NitriteId> flowable = repository.insert(e1, e2);
        assertEquals(0, repository.find().size().blockingGet().intValue());

        Disposable subscribe = repository.observe(EventType.Insert)
            .subscribe(collectionEventInfo -> {
                System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
            });

        assertEquals(0, repository.find().size().blockingGet().intValue());
        TestSubscriber<NitriteId> test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(2, repository.find().size().blockingGet().intValue());

        subscribe.dispose();

        flowable = repository.update(where("name").eq("John Doe"),
            new Employee("RxNitrite", 36));
        subscribe = repository.observe(EventType.Update)
            .subscribe(collectionEventInfo -> {
                System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
            });

        assertEquals(2, repository.find().size().blockingGet().intValue());
        assertEquals(repository.find(where("name").eq("RxNitrite")).size().blockingGet().intValue(), 0);
        test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(repository.find(where("name").eq("RxNitrite")).size().blockingGet().intValue(), 1);
        assertEquals(2, repository.find().size().blockingGet().intValue());

        flowable = repository.update(new Employee("RxNitrite", 40));
        assertEquals(repository.find(where("name").eq("RxNitrite")).firstOrNull().blockingGet().getAge().longValue(), 36L);
        test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(repository.find(where("name").eq("RxNitrite")).firstOrNull().blockingGet().getAge().longValue(), 40L);

        Employee employee = new Employee();
        employee.setName("Iron Man");
        employee.setAge(99);
        flowable = repository.update(where("name").eq("RxNitrite"), employee, true);
        assertNull(repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet());
        test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(99, repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet().getAge().longValue());

        flowable = repository.update(where("name").eq("Iron Man"), Document.createDocument("age", 100));
        assertEquals(99, repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet().getAge().longValue());
        test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(100, repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet().getAge().longValue());

        flowable = repository.update(where("name").eq("Iron Man"), Document.createDocument("age", 200), true);
        assertEquals(100, repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet().getAge().longValue());
        test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(200, repository.find(where("name").eq("Iron Man")).firstOrNull().blockingGet().getAge().longValue());

        subscribe.dispose();
    }

    @Test
    public void testUpsert() throws InterruptedException {
        AtomicInteger insertCount = new AtomicInteger(0);
        AtomicInteger updateCount = new AtomicInteger(0);

        Disposable insertSubscriber = repository.observe(EventType.Insert)
            .subscribe(collectionEventInfo -> {
                System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
                insertCount.incrementAndGet();
            });

        Disposable updateSubscriber = repository.observe(EventType.Update)
            .subscribe(collectionEventInfo -> {
                System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
                updateCount.incrementAndGet();
            });


        assertEquals(repository.find().size().blockingGet().intValue(), 0);

        final PodamFactory factory = new PodamFactoryImpl();
        ExecutorService pool = ThreadPoolManager.getThreadPool(2, "RxObjectRepositoryTest");
        final CountDownLatch latch = new CountDownLatch(10000);
        String[] names = new String[]{"Iron Man", "Captain America", "Thor", "Hulk", "Black Widow", "Black Panther"};

        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < 10000; i++) {
            pool.submit(() -> {
                int index = count.getAndIncrement() % 6;
                Employee employee = factory.manufacturePojoWithFullData(Employee.class);
                employee.setName(names[index]);
                TestSubscriber<NitriteId> testSubscriber = repository.update(employee, true).test();
                testSubscriber.awaitTerminalEvent();
                testSubscriber.assertComplete();
                testSubscriber.assertNoErrors();
                latch.countDown();
            });
        }

        latch.await();

        assertEquals(6, repository.find().size().blockingGet().intValue());
        assertTrue(insertCount.get() <= 6);
        assertTrue(updateCount.get() <= 10000 - insertCount.get());

        insertSubscriber.dispose();
        updateSubscriber.dispose();
        pool.shutdown();
    }

    @Test
    public void testRemove() {
        Flowable<NitriteId> flowable = repository.insert(testData.toArray(new Employee[0]));
        assertEquals(0, repository.find().size().blockingGet().intValue());
        TestSubscriber<NitriteId> test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(2, repository.find().size().blockingGet().intValue());

        Disposable subscribe = repository.observe()
            .subscribe(collectionEventInfo -> {
                System.out.println(collectionEventInfo.getEventType() + " of " + collectionEventInfo.getItem());
            });

        repository.remove(Filter.ALL).test();
        assertEquals(0, repository.find().size().blockingGet().intValue());
        subscribe.dispose();
    }

}
