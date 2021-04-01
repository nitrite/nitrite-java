package org.dizitart.no2.filters;

import org.junit.Test;

import static org.junit.Assert.*;

public class FluentFilterTest {
    @Test
    public void testEq() {
        assertTrue(((EqualsFilter) FluentFilter.where("field").eq("value")).getValue() instanceof String);
        assertFalse(((EqualsFilter) FluentFilter.where("field").eq("value")).getObjectFilter());
        assertEquals("field", ((EqualsFilter) FluentFilter.where("field").eq("value")).getField());
    }

    @Test
    public void testNotEq() {
        assertTrue(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getValue() instanceof String);
        assertFalse(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getObjectFilter());
        assertEquals("field", ((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getField());
    }

    @Test
    public void testText() {
        assertEquals("value", ((TextFilter) FluentFilter.where("field").text("value")).getStringValue());
        assertFalse(((TextFilter) FluentFilter.where("field").text("value")).getObjectFilter());
        assertEquals("field", ((TextFilter) FluentFilter.where("field").text("value")).getField());
    }

    @Test
    public void testRegex() {
        assertEquals("field", ((RegexFilter) FluentFilter.where("field").regex("value")).getField());
        assertFalse(((RegexFilter) FluentFilter.where("field").regex("value")).getObjectFilter());
        assertEquals("FieldBasedFilter(field=field, value=value, processed=true)",
            FluentFilter.where("field").regex("value").toString());
    }
}

