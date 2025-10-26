package org.dizitart.no2.integration.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    @Test
    public void testMultipleIndexesOrFilterDuplicates() {
        NitriteCollection items = db.getCollection("items");
        items.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "field_a");
        items.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "field_b");

        Document doc = Document.createDocument();
        doc.put("field_a", "A");
        doc.put("field_b", "B");
        items.insert(doc);

        Filter aFilter = FluentFilter.where("field_a").eq("A");
        Filter bFilter = FluentFilter.where("field_b").eq("B");

        Filter orFilter = Filter.or(aFilter, bFilter);

        DocumentCursor cursor = items.find(orFilter);
        Iterator<Document> docIter = cursor.iterator();

        List<Long> matches = new ArrayList<>();
        while (docIter.hasNext()) {
            Document match = docIter.next();
            long id = match.getId().getIdValue();
            matches.add(id);
        }
        assertEquals("Single document must yield single match", 1, matches.size());
    }
}
