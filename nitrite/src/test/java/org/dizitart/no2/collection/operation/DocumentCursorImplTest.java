package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertTrue;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

public class DocumentCursorImplTest {
    @Test
    public void testSort() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        assertTrue((new DocumentCursorImpl(new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()))))
            .sort("field", SortOrder.Ascending, null, NullOrder.First).isEmpty());
    }

    @Test
    public void testSkipLimit() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        assertTrue((new DocumentCursorImpl(new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()))))
            .skipLimit(1L, 1L).isEmpty());
    }

    @Test
    public void testProject() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        DocumentCursorImpl documentCursorImpl = new DocumentCursorImpl(
            new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId())));
        assertTrue(documentCursorImpl.project(Document.createDocument()).isEmpty());
    }

    @Test
    public void testJoin() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        DocumentCursorImpl documentCursorImpl = new DocumentCursorImpl(
            new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId())));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream3 = new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId()));
        DocumentCursorImpl foreignCursor = new DocumentCursorImpl(
            new FilteredRecordStream(recordStream3, Filter.byId(NitriteId.newId())));
        assertTrue(documentCursorImpl.join(foreignCursor, new Lookup()).isEmpty());
    }
}

