package org.dizitart.no2.collection.operation;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class BoundedDocumentStreamTest {
    @Test
    public void testBoundedIteratorHasNext() {
        assertTrue(
            (new BoundedDocumentStream.BoundedIterator<>(new ArrayIterator<>(new Object[]{"foo", "foo", "foo"}),
                1L, 3L)).hasNext());
    }

    @Test
    public void testBoundedIteratorNext() {
        assertEquals("foo", (new BoundedDocumentStream.BoundedIterator<>(new BoundedDocumentStream.BoundedIterator<>(
            new ArrayIterator<>(new Object[]{"foo", "foo", "foo"}), 1L, 3L), 1L, 3L)).next());
        assertEquals("foo",
            (new BoundedDocumentStream.BoundedIterator<>(new ArrayIterator<>(new Object[]{"foo", "foo", "foo"}),
                1L, 3L)).next());
        assertThrows(NoSuchElementException.class, () -> (new BoundedDocumentStream.BoundedIterator<>(
            new ArrayIterator<>(new Object[]{"foo", "foo", "foo"}), 1L, 0L)).next());
    }

    @Test
    public void testConstructor() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        assertTrue(
            (new BoundedDocumentStream(new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId())), 1L, 1L))
                .isEmpty());
    }

    @Test
    public void testConstructor2() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        assertThrows(ValidationException.class,
            () -> new BoundedDocumentStream(new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId())), -1L,
                1L));
    }

    @Test
    public void testConstructor3() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        assertThrows(ValidationException.class,
            () -> new BoundedDocumentStream(new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId())), 1L,
                -1L));
    }

    @Test
    public void testIterator() {
        assertTrue((new BoundedDocumentStream(null, 1L, 1L)).iterator() instanceof BoundedDocumentStream.BoundedIterator);
    }

    @Test
    public void testIterator2() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        BoundedDocumentStream recordStream2 = new BoundedDocumentStream(
            new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId())), 1L, 1L);
        assertTrue(
            (new BoundedDocumentStream(new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId())), 1L, 1L))
                .iterator() instanceof BoundedDocumentStream.BoundedIterator);
    }

    @Test
    public void testIterator3() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        assertTrue(
            (new BoundedDocumentStream(new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId())), 1L, 1L))
                .iterator() instanceof BoundedDocumentStream.BoundedIterator);
    }
}

