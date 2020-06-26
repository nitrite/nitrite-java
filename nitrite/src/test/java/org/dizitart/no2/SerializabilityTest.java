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

package org.dizitart.no2;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee
 */
public class SerializabilityTest {
    private Nitrite db;
    private NitriteCollection collection;
    private File dbFile;

    @Before
    public void setUp() {
        dbFile = new File(getRandomTempDbFile());
        db = NitriteBuilder.get()
            .filePath(dbFile)
            .compressed()
            .openOrCreate();
        collection = db.getCollection("test");
    }

    @After
    public void tearDown() {
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test(expected = ValidationException.class)
    public void testSerializabilityValidation() {
        for (int i = 0; i < 5; i++) {
            Document doc = Document.createDocument();
            doc.put("key", i);
            doc.put("data", new NotSerializableClass(Integer.toString(i)));
            collection.insert(doc);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Write " + i + " completed");
        }
    }

    @Test
    public void testSerializablity() {
        for (int i = 0; i < 5; i++) {
            Document doc = Document.createDocument();
            doc.put("key", i);
            doc.put("data", new SerializableClass(Integer.toString(i)));
            collection.insert(doc);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Write " + i + " completed");
        }
    }

    @Data
    public static class NotSerializableClass {
        private String myId;

        public NotSerializableClass(String myId) {
            this.myId = myId;
        }
    }

    @Data
    public static class SerializableClass implements Serializable {
        private String myId;

        public SerializableClass(String myId) {
            this.myId = myId;
        }
    }
}


