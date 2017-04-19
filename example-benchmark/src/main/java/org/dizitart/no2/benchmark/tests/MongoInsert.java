package org.dizitart.no2.benchmark.tests;


import org.bson.Document;

/**
 * @author Anindya Chatterjee.
 */
public class MongoInsert extends BaseMongoBenchMark {

    @Override
    public void runTest() {
        for (Document document : documents) {
            personCollection.insertOne(document);
        }
    }
}
