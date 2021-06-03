package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class EqualsFilterTest {
    @Test
    public void testConstructor() {
        EqualsFilter actualEqualsFilter = new EqualsFilter("Field", "Value");
        assertEquals("(Field == Value)", actualEqualsFilter.toString());
        assertFalse(actualEqualsFilter.getObjectFilter());
        assertEquals("Field", actualEqualsFilter.getField());
    }

    @Test
    public void testToString() {
        EqualsFilter equalsFilter = new EqualsFilter("Field", "Value");
        assertEquals("(Field == Value)", equalsFilter.toString());
        assertEquals("(!((Field == Value)))", equalsFilter.not().toString());
    }

    @Test
    public void testToString2() {
        EqualsFilter equalsFilter = new EqualsFilter("Field", "Value");
        equalsFilter.setProcessed(true);
        equalsFilter.setObjectFilter(true);
        assertEquals("(Field == Value)", equalsFilter.toString());
    }

    @Test
    public void testApply() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<>();
        pair.setSecond(Document.createDocument());
        assertFalse(equalsFilter.apply(pair));
        assertTrue(equalsFilter.getValue() instanceof String);
    }

    @Test
    public void testApply2() {
        EqualsFilter equalsFilter = new EqualsFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertFalse(equalsFilter.apply(new Pair<>(first, Document.createDocument())));
        assertTrue(equalsFilter.getValue() instanceof String);
    }
}

