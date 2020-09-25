package org.dizitart.no2.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FluentFilterTest {
    @Test
    public void testEq() {
        assertFalse(((EqualsFilter) FluentFilter.where("field").eq("value")).getOnIdField());
        assertTrue(((EqualsFilter) FluentFilter.where("field").eq("value")).getValue() instanceof String);
        assertFalse(((EqualsFilter) FluentFilter.where("field").eq("value")).getObjectFilter());
        assertFalse(((EqualsFilter) FluentFilter.where("field").eq("value")).getIsFieldIndexed());
        assertEquals("field", ((EqualsFilter) FluentFilter.where("field").eq("value")).getField());
    }

    @Test
    public void testNotEq() {
        assertFalse(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getOnIdField());
        assertTrue(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getValue() instanceof String);
        assertFalse(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getObjectFilter());
        assertFalse(((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getIsFieldIndexed());
        assertEquals("field", ((NotEqualsFilter) FluentFilter.where("field").notEq("value")).getField());
    }

    @Test
    public void testText() {
        assertEquals("value", ((TextFilter) FluentFilter.where("field").text("value")).getStringValue());
        assertFalse(((TextFilter) FluentFilter.where("field").text("value")).getOnIdField());
        assertFalse(((TextFilter) FluentFilter.where("field").text("value")).getObjectFilter());
        assertFalse(((TextFilter) FluentFilter.where("field").text("value")).getIsFieldIndexed());
        assertEquals("field", ((TextFilter) FluentFilter.where("field").text("value")).getField());
    }

    @Test
    public void testRegex() {
        assertEquals("field", ((RegexFilter) FluentFilter.where("field").regex("value")).getField());
        assertFalse(((RegexFilter) FluentFilter.where("field").regex("value")).getObjectFilter());
        assertEquals("FieldBasedFilter(field=field, value=value, processed=true)",
            ((RegexFilter) FluentFilter.where("field").regex("value")).toString());
    }
}

