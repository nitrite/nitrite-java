package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RegexFilterTest {
    @Test
    public void testConstructor() {
        RegexFilter actualRegexFilter = new RegexFilter("Field", "42");
        assertEquals("(Field regex 42)", actualRegexFilter.toString());
        assertEquals("Field", actualRegexFilter.getField());
        assertFalse(actualRegexFilter.getObjectFilter());
    }

    @Test
    public void testToString() {
        RegexFilter regexFilter = new RegexFilter("Field", "42");
        assertEquals("(Field regex 42)", regexFilter.toString());
        assertEquals("(!((Field regex 42)))", regexFilter.not().toString());
    }

    @Test
    public void testToString2() {
        RegexFilter regexFilter = new RegexFilter("Field", "42");
        regexFilter.setProcessed(true);
        regexFilter.setObjectFilter(true);
        assertEquals("(Field regex 42)", regexFilter.toString());
    }

    @Test
    public void testApply() {
        RegexFilter regexFilter = new RegexFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<>();
        pair.setSecond(Document.createDocument());
        assertFalse(regexFilter.apply(pair));
    }

    @Test
    public void testApply2() {
        RegexFilter regexFilter = new RegexFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertFalse(regexFilter.apply(new Pair<>(first, Document.createDocument())));
    }
}

