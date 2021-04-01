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
        RegexFilter actualRegexFilter = new RegexFilter("field", "value");
        assertEquals("field", actualRegexFilter.getField());
        assertFalse(actualRegexFilter.getObjectFilter());
        assertEquals("FieldBasedFilter(field=field, value=value, processed=true)", actualRegexFilter.toString());
    }

    @Test
    public void testApply() {
        RegexFilter regexFilter = new RegexFilter("field", "value");
        Pair<NitriteId, Document> pair = new Pair<NitriteId, Document>();
        pair.setSecond(Document.createDocument());
        assertFalse(regexFilter.apply(pair));
    }

    @Test
    public void testApply2() {
        RegexFilter regexFilter = new RegexFilter("field", "value");
        NitriteId first = NitriteId.newId();
        assertFalse(regexFilter.apply(new Pair<NitriteId, Document>(first, Document.createDocument())));
    }
}

