/*
 * Copyright 2017 Nitrite author or authors.
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
 */

package org.dizitart.no2.benchmark.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.dizitart.no2.benchmark.core.Benchmark;
import org.dizitart.no2.benchmark.data.Person;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseMongoBenchMark implements Benchmark {
    private MongoClient mongoClient;
    private MongoDatabase db;
    MongoCollection<Document> personCollection;
    private BenchmarkTestHelper testHelper = new BenchmarkTestHelper();
    List<Document> documents;

    @Override
    public void beforeTest() {
        final MongoCredential credential =
                MongoCredential.createCredential("bench", "benchmark", "bench".toCharArray());
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        mongoClient = new MongoClient(serverAddress, new ArrayList<MongoCredential>() {{ add(credential); }});
        db = mongoClient.getDatabase("benchmark");

        Person[] personList = testHelper.loadData();
        documents = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Person person : personList) {
            StringWriter writer = new StringWriter();
            try {
                objectMapper.writeValue(writer, person);
                Document document = Document.parse(writer.toString());
                documents.add(document);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeRun() {
        personCollection = db.getCollection("person");
    }

    @Override
    public void afterRun() {
        if (personCollection != null) {
            personCollection.drop();
        }
    }

    @Override
    public void afterTest() {
        if(db != null) {
            db.drop();
        }
        mongoClient.close();
    }
}
