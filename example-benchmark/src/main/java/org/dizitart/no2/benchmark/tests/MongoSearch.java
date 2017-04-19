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
