package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

public class FilteredRecordStreamTest {
    @Test
    public void testFilteredIteratorHasNext() {
        ArrayIterator<Pair<NitriteId, Document>> iterator = new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{});
        FilteredRecordStream.FilteredIterator iterator1 = new FilteredRecordStream.FilteredIterator(iterator,
            Filter.byId(NitriteId.newId()));
        assertFalse((new FilteredRecordStream.FilteredIterator(iterator1, Filter.byId(NitriteId.newId()))).hasNext());
    }

    @Test
    public void testIterator() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        assertTrue((new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId())))
            .iterator() instanceof FilteredRecordStream.FilteredIterator);
    }

    @Test
    public void testFilteredIteratorHasNext2() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        pair.setSecond(Document.createDocument());
        ArrayIterator<Pair<NitriteId, Document>> iterator = new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair});
        assertFalse((new FilteredRecordStream.FilteredIterator(iterator, Filter.byId(NitriteId.newId()))).hasNext());
    }

    @Test
    public void testFilteredIteratorHasNext3() {
        ArrayIterator<Pair<NitriteId, Document>> iterator = new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{});
        assertFalse((new FilteredRecordStream.FilteredIterator(iterator, Filter.byId(NitriteId.newId()))).hasNext());
    }
}

