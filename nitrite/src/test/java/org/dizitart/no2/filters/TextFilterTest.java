package org.dizitart.no2.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.dizitart.no2.exceptions.FilterException;
import org.junit.Test;

public class TextFilterTest {
    @Test
    public void testFindIndexedIdSet() {
        assertEquals(0, (new TextFilter("field", "value")).findIndexedIdSet().size());
    }

    @Test
    public void testFindIndexedIdSet2() {
        TextFilter textFilter = new TextFilter("field", "value");
        textFilter.setNitriteIndexer(null);
        textFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> textFilter.findIndexedIdSet());
    }

    @Test
    public void testFindIndexedIdSet3() {
        TextFilter textFilter = new TextFilter("field", "value");
        textFilter.setIsFieldIndexed(true);
        assertThrows(FilterException.class, () -> textFilter.findIndexedIdSet());
    }
}

