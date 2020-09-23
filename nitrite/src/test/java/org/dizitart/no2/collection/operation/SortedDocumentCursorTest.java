package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

public class SortedDocumentCursorTest {
    @Test
    public void testIterator() {
        assertFalse(
            (new SortedDocumentCursor("field", SortOrder.Ascending, null, NullOrder.First, null)).iterator().hasNext());
    }

    @Test
    public void testIterator2() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        assertFalse((new SortedDocumentCursor("field", SortOrder.Ascending, null, NullOrder.First,
            new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId())))).iterator().hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", null, null, NullOrder.Default, arrayIterator))
            .hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor2() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, NullOrder.Default,
            arrayIterator)).hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor3() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, NullOrder.First,
            arrayIterator)).hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor4() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", null, null, NullOrder.First, arrayIterator))
            .hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor5() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse(
            (new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, null, arrayIterator))
                .hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor6() {
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, NullOrder.First,
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{}))).hasNext());
    }

    @Test
    public void testSortedDocumentIteratorConstructor7() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        ArrayIterator<Pair<NitriteId, Document>> arrayIterator = new ArrayIterator<Pair<NitriteId, Document>>(
            new Pair[]{pair, pair1, new Pair<NitriteId, Document>()});
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", null, null, NullOrder.Last, arrayIterator))
            .hasNext());
        assertFalse(arrayIterator.hasNext());
    }

    @Test
    public void testSortedDocumentIteratorHasNext() {
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        assertFalse((new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, NullOrder.First,
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()})))
            .hasNext());
    }

    @Test
    public void testSortedDocumentIteratorNext() {
        NitriteId first = NitriteId.newId();
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>(first, Document.createDocument());
        Pair<NitriteId, Document> pair1 = new Pair<NitriteId, Document>();
        assertSame(pair,
            (new SortedDocumentCursor.SortedDocumentIterator("field", SortOrder.Ascending, null, NullOrder.First,
                new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()})))
                .next());
    }
}

