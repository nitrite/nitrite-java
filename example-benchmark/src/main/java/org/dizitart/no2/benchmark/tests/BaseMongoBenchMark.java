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
