package org.dizitart.no2.filters;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FluentFilterTest {
    @Test
    public void testEq() {
        NitriteFilter actualEqResult = FluentFilter.where("Field").eq("Value");
        assertEquals("(Field == Value)", actualEqResult.toString());
        assertFalse(actualEqResult.getObjectFilter());
        assertEquals("Field", ((EqualsFilter) actualEqResult).getField());
    }

    @Test
    public void testNotEq() {
        NitriteFilter actualNotEqResult = FluentFilter.where("Field").notEq("Value");
        assertEquals("(Field != Value)", actualNotEqResult.toString());
        assertFalse(actualNotEqResult.getObjectFilter());
        assertEquals("Field", ((NotEqualsFilter) actualNotEqResult).getField());
    }

    @Test
    public void testText() {
        NitriteFilter actualTextResult = FluentFilter.where("Field").text("42");
        assertEquals("Field", ((TextFilter) actualTextResult).getField());
        assertFalse(actualTextResult.getObjectFilter());
        assertEquals("42", ((TextFilter) actualTextResult).getStringValue());
    }

    @Test
    public void testRegex() {
        NitriteFilter actualRegexResult = FluentFilter.where("Field").regex("42");
        assertEquals("(Field regex 42)", actualRegexResult.toString());
        assertEquals("Field", ((RegexFilter) actualRegexResult).getField());
        assertFalse(actualRegexResult.getObjectFilter());
    }

    @Test
    public void testElemMatch() {
        assertFalse(FluentFilter.where("Field").elemMatch(mock(Filter.class)).getObjectFilter());
    }
}

