package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

public class ExistsFilterTest {
    private static boolean apply(Filter filter, Document document) {
        return filter.apply(new Pair<>(NitriteId.newId(), document));
    }

    @Test
    public void testConstructor() {
        ExistsFilter filter = new ExistsFilter("Field");
        assertEquals("Field", filter.getField());
        assertNull(filter.getValue());
        assertFalse(filter.getObjectFilter());
    }

    @Test
    public void testToString() {
        ExistsFilter filter = new ExistsFilter("Field");
        assertEquals("(Field exists)", filter.toString());
        assertEquals("(!((Field exists)))", filter.not().toString());
    }

    @Test
    public void testFluentFilter() {
        assertTrue(where("Field").exists() instanceof ExistsFilter);
        assertEquals("(Field exists)", where("Field").exists().toString());
    }

    @Test
    public void testFieldPresent() {
        assertTrue(apply(where("firstName").exists(), createDocument("firstName", "fn1")));
    }

    @Test
    public void testFieldAbsent() {
        assertFalse(apply(where("lastName").exists(), createDocument("firstName", "fn1")));
    }

    @Test
    public void testEmptyDocument() {
        assertFalse(apply(where("firstName").exists(), createDocument()));
    }

    @Test
    public void testNullValueIsPresent() {
        // a field explicitly set to null exists - that is the whole point of
        // the filter, `eq(null)` cannot tell it apart from a missing field
        Document document = createDocument("firstName", null);
        assertTrue(apply(where("firstName").exists(), document));
        assertFalse(apply(where("firstName").exists().not(), document));
    }

    @Test
    public void testEmbeddedField() {
        Document document = createDocument("name", "n1")
            .put("address", createDocument("city", "c1").put("pin", 123456));

        assertTrue(apply(where("address").exists(), document));
        assertTrue(apply(where("address.city").exists(), document));
        assertTrue(apply(where("address.pin").exists(), document));
        assertFalse(apply(where("address.street").exists(), document));
        assertFalse(apply(where("addr.city").exists(), document));
    }

    @Test
    public void testNot() {
        Document document = createDocument("firstName", "fn1");
        assertFalse(apply(where("firstName").exists().not(), document));
        assertTrue(apply(where("lastName").exists().not(), document));
    }

    @Test
    public void testAndOr() {
        Document document = createDocument("firstName", "fn1").put("age", 30);

        assertTrue(apply(where("firstName").exists().and(where("age").exists()), document));
        assertFalse(apply(where("firstName").exists().and(where("lastName").exists()), document));
        assertTrue(apply(where("lastName").exists().or(where("age").exists()), document));
        assertFalse(apply(where("lastName").exists().or(where("email").exists()), document));
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(where("Field").exists(), where("Field").exists());
        assertEquals(where("Field").exists().hashCode(), where("Field").exists().hashCode());
        assertNotEquals(where("Field").exists(), where("Other").exists());
    }
}
