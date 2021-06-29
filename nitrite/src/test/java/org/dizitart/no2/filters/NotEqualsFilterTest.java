package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class NotEqualsFilterTest {
    @Test
    public void testConstructor() {
        NotEqualsFilter actualNotEqualsFilter = new NotEqualsFilter("Field", "Value");
        assertEquals("(Field != Value)", actualNotEqualsFilter.toString());
        assertFalse(actualNotEqualsFilter.getObjectFilter());
        assertEquals("Field", actualNotEqualsFilter.getField());
    }

    @Test
    public void testToString() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("Field", "Value");
        assertEquals("(Field != Value)", notEqualsFilter.toString());
        assertEquals("(!((Field != Value)))", notEqualsFilter.not().toString());
    }

    @Test
    public void testToString2() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("Field", "Value");
        notEqualsFilter.setProcessed(true);
        notEqualsFilter.setObjectFilter(true);
        assertEquals("(Field != Value)", notEqualsFilter.toString());
    }

    @Test
    public void testApply() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertTrue(notEqualsFilter.apply(new Pair<>(first, Document.createDocument())));
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }

    @Test
    public void testApply2() {
        NotEqualsFilter notEqualsFilter = new NotEqualsFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<>();
        pair.setSecond(Document.createDocument());
        assertTrue(notEqualsFilter.apply(pair));
        assertTrue(notEqualsFilter.getValue() instanceof String);
    }
}

