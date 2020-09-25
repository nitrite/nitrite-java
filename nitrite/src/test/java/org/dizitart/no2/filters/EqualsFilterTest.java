package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.index.NitriteTextIndexer;
import org.dizitart.no2.index.NonUniqueIndexer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class EqualsFilterTest {
    @Test
    public void testFindIndexedIdSet() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        equalsFilter.setIndexer(new NitriteTextIndexer());
        equalsFilter.setIsFieldIndexed(null);
        assertEquals(0, equalsFilter.findIndexedIdSet().size());
    }

    @Test
    public void testFindIndexedIdSet2() {
        EqualsFilter equalsFilter = new EqualsFilter("field", new NonUniqueIndexer());
        equalsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> equalsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet3() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        equalsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> equalsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet4() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        equalsFilter.setIsFieldIndexed(true);
        equalsFilter.setIndexer(new NitriteTextIndexer());
        equalsFilter.setIsFieldIndexed(null);
        assertEquals(0, equalsFilter.findIndexedIdSet().size());
    }

    @Test
    public void testFindIndexedIdSet5() {
        EqualsFilter equalsFilter = new EqualsFilter("field", null);
        equalsFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> equalsFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet6() {
        assertEquals(0, (new EqualsFilter("field", "value")).findIndexedIdSet().size());
    }

    @Test
    public void testFindIdSet() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        assertEquals(0, equalsFilter.findIdSet(new InMemoryMap<NitriteId, Document>("mapName", null)).size());
    }

    @Test(expected = InvalidIdException.class)
    public void testFindIdSet2() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        equalsFilter.setOnIdField(true);
        equalsFilter.findIdSet(new InMemoryMap<>("mapName", null));
        assertTrue(equalsFilter.getValue() instanceof String);
    }

    @Test
    public void testFindIdSet3() {
        EqualsFilter equalsFilter = new EqualsFilter("field", 42);
        equalsFilter.setOnIdField(true);
        assertEquals(0, equalsFilter.findIdSet(new InMemoryMap<NitriteId, Document>("mapName", null)).size());
        assertTrue(equalsFilter.getValue() instanceof Integer);
    }

    @Test
    public void testApply() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        pair.setSecond(Document.createDocument());
        assertFalse(equalsFilter.apply(pair));
        assertTrue(equalsFilter.getValue() instanceof String);
    }

    @Test
    public void testApply2() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertFalse(equalsFilter.apply(new Pair<NitriteId, Document>(first, Document.createDocument())));
        assertTrue(equalsFilter.getValue() instanceof String);
    }

    @Test
    public void testSetIsFieldIndexed() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        equalsFilter.setIsFieldIndexed(true);
        assertTrue(equalsFilter.getIsFieldIndexed());
    }

    @Test
    public void testSetIsFieldIndexed2() {
        EqualsFilter equalsFilter = new EqualsFilter("field", 42);
        equalsFilter.setObjectFilter(true);
        equalsFilter.setIndexer(null);
        equalsFilter.setIsFieldIndexed(true);
        assertTrue(equalsFilter.getIsFieldIndexed());
    }

    @Test
    public void testSetIsFieldIndexed3() {
        EqualsFilter equalsFilter = new EqualsFilter("field", 42);
        equalsFilter.setIndexer(new NitriteTextIndexer());
        equalsFilter.setIsFieldIndexed(true);
        assertTrue(equalsFilter.getValue() instanceof Integer);
        assertTrue(equalsFilter.getIsFieldIndexed());
    }
}

