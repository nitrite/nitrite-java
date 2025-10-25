package org.dizitart.no2.integration.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IssueTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("myCollection");
    }

    @After
    public void tearDown() {
        if (collection != null) {
            collection.close();
        }
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testOriginalIssue() {
        Document doc = Document.createDocument("value", 42);
        collection.insert(doc);

        assertEquals(1, collection.find(FluentFilter.where("value").eq(42L)).size());
        assertEquals(1, collection.find(FluentFilter.where("value").lte(42L)).size());
        assertEquals(1, collection.find(FluentFilter.where("value").gte(42L)).size());

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        assertEquals(1, collection.find(FluentFilter.where("value").eq(42L)).size());
        assertEquals(1, collection.find(FluentFilter.where("value").lte(42L)).size());
        assertEquals(1, collection.find(FluentFilter.where("value").gte(42L)).size());
    }
}
