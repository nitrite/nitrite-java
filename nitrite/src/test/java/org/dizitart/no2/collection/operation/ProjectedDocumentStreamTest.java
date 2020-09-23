package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertEquals;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

public class ProjectedDocumentStreamTest {
    @Test
    public void testToString() {
        assertEquals("[]", (new ProjectedDocumentStream(null, Document.createDocument())).toString());
    }

    @Test
    public void testToString2() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        assertEquals("[]", (new ProjectedDocumentStream(recordStream2, Document.createDocument())).toString());
    }
}

