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

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author Anindya Chatterjee.
 */
public class MongoSearch extends BaseMongoBenchMark {

    @Override
    public void beforeRun() {
        super.beforeRun();
        IndexOptions firstNameOption = new IndexOptions();
        firstNameOption.unique(false);
        personCollection.createIndex(new Document("firstName", 1), firstNameOption);
        personCollection.createIndex(new Document("personalNote", "text"));

        personCollection.insertMany(documents);
    }

    @Override
    public void runTest() {
        FindIterable<Document> documents = personCollection.find(eq("firstName", "abcd"));
        assert documents != null;

        documents = personCollection.find(new BasicDBObject("$text", new BasicDBObject("$search", "Lorem")));
        assert documents != null;
    }
}
