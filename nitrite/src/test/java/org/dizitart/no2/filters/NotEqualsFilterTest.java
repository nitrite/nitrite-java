package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NotEqualsFilterTest {

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

