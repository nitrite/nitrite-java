package org.dizitart.no2.filters;

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class BetweenFilterTest {
    @Test
    public void testConstructor() {
        BetweenFilter<Object> actualBetweenFilter = new BetweenFilter<Object>("field",
            new BetweenFilter.Bound<>("lowerBound", "upperBound"));
        Filter lhs = actualBetweenFilter.getLhs();
        assertTrue(lhs instanceof LesserEqualFilter);
        Filter rhs = actualBetweenFilter.getRhs();
        assertTrue(rhs instanceof GreaterEqualFilter);
        Boolean actualOnIdField = ((LesserEqualFilter) lhs).getOnIdField();
        assertFalse(actualBetweenFilter.getObjectFilter());
        assertFalse(((GreaterEqualFilter) rhs).getIsFieldIndexed());
        assertFalse(((GreaterEqualFilter) rhs).getObjectFilter());
        assertFalse(actualOnIdField);
        assertEquals("field", ((GreaterEqualFilter) rhs).getField());
        assertTrue(((GreaterEqualFilter) rhs).getComparable() instanceof String);
        assertFalse(((GreaterEqualFilter) rhs).getOnIdField());
        assertFalse(((LesserEqualFilter) lhs).getIsFieldIndexed());
        assertFalse(((LesserEqualFilter) lhs).getObjectFilter());
        assertEquals("field", ((LesserEqualFilter) lhs).getField());
        assertTrue(((LesserEqualFilter) lhs).getComparable() instanceof String);
    }

    @Test
    public void testConstructor2() {
        assertThrows(ValidationException.class,
            () -> new BetweenFilter<Object>("field", new BetweenFilter.Bound<Object>("lowerBound", null)));
    }

    @Test
    public void testConstructor3() {
        assertThrows(ValidationException.class, () -> new BetweenFilter<Object>("field", null));
    }

    @Test
    public void testConstructor4() {
        assertThrows(ValidationException.class,
            () -> new BetweenFilter<Object>("field", new BetweenFilter.Bound<Object>(null, "upperBound")));
    }
}

