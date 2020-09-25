package org.dizitart.no2.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.NitriteTextIndexer;
import org.dizitart.no2.index.NonUniqueIndexer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

public class NotEqualsFilterTest {
    @Test
    public void testFindIndexedIdSet() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", null);
        notEqualsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> notEqualsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet2() {
        assertEquals(0, (new NotEqualsFilter("field", "value")).findIndexedIdSet().size());
    }

    @Test
    public void testFindIndexedIdSet3() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", new NonUniqueIndexer());
        notEqualsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> notEqualsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet4() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> notEqualsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet5() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setIndexer(new NitriteTextIndexer());
        notEqualsFilter.setIsFieldIndexed(null);
        assertEquals(0, notEqualsFilter.findIndexedIdSet().size());
    }

    @Test
    public void testFindIndexedIdSet6() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setIsFieldIndexed(true);
        notEqualsFilter.setIndexer(new NitriteTextIndexer());
        notEqualsFilter.setIsFieldIndexed(null);
        assertEquals(0, notEqualsFilter.findIndexedIdSet().size());
    }

    @Test
    public void testFindIdSet() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        assertEquals(0, notEqualsFilter.findIdSet(new InMemoryMap<NitriteId, Document>("mapName", null)).size());
    }

    @Test
    public void testFindIdSet2() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", 42);
        notEqualsFilter.setOnIdField(true);
        assertEquals(0, notEqualsFilter.findIdSet(new InMemoryMap<NitriteId, Document>("mapName", null)).size());
        assertTrue(notEqualsFilter.getValue() instanceof Integer);
    }

    @Test
    public void testFindIdSet3() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setOnIdField(true);
        notEqualsFilter.findIdSet(new InMemoryMap<NitriteId, Document>("mapName", null));
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }

    @Test
    public void testApply() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertTrue(notEqualsFilter.apply(new Pair<NitriteId, Document>(first, Document.createDocument())));
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }

    @Test
    public void testApply2() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        pair.setSecond(Document.createDocument());
        assertTrue(notEqualsFilter.apply(pair));
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }

    @Test
    public void testSetIsFieldIndexed() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", 42);
        notEqualsFilter.setIndexer(new NitriteTextIndexer());
        notEqualsFilter.setIsFieldIndexed(true);
        assertTrue(notEqualsFilter.getValue() instanceof Integer);
        assertTrue(notEqualsFilter.getIsFieldIndexed());
    }

    @Test
    public void testSetIsFieldIndexed2() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", 42);
        notEqualsFilter.setObjectFilter(true);
        notEqualsFilter.setIndexer(null);
        notEqualsFilter.setIsFieldIndexed(true);
        assertTrue(notEqualsFilter.getIsFieldIndexed());
    }

    @Test
    public void testSetIsFieldIndexed3() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setIsFieldIndexed(true);
        assertTrue(notEqualsFilter.getIsFieldIndexed());
    }

    @Test
    public void testSetIsFieldIndexed4() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        notEqualsFilter.setIndexer(new NitriteTextIndexer());
        notEqualsFilter.setIsFieldIndexed(true);
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }
}

