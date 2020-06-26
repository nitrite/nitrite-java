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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.junit.Before;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class RxBaseTest {
    List<Employee> testData;
    RxNitrite db;
    String dbPath = getRandomTempDbFile();

    private static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir")
            + File.separator + "rx-nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }

    @Before
    public void setUp() {
        db = new RxNitrite(NitriteBuilder.get()
            .filePath(dbPath)
            .openOrCreate("test-user", "test-password"));

        Employee e1 = new Employee("John Doe", 35);
        Employee e2 = new Employee("Jane Doe", 30);
        testData = Flowable.fromArray(e1, e2).toList().blockingGet();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Employee implements Mappable {
        @Id
        private String name;
        private Integer age;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument("name", name)
                .put("age", age);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            name = document.get("name", String.class);
            age = document.get("age", Integer.class);
        }
    }

    static abstract class BaseSubscriber<T> implements Subscriber<T> {

        @Override
        public void onSubscribe(Subscription s) {
            log.info("Subscribed to {}", s);
        }

        @Override
        public void onError(Throwable t) {
            log.error("Error in subscriber", t);
        }

        @Override
        public void onComplete() {
            log.info("Subscriber completed");
        }
    }
}
