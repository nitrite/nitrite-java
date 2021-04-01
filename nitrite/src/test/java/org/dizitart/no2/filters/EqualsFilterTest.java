package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EqualsFilterTest {

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

