package org.dizitart.no2.util;

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.util.ReflectionUtils.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ReflectionUtilsNegativeTest {
    @Test(expected = ValidationException.class)
    public void testGetAnnotatedFieldsNullType() {
        assertEquals(getAnnotatedFields(null,
                Test.class).size(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testGetAnnotatedFieldsNullAnnotation() {
        assertEquals(getAnnotatedFields(ClassWithNoAnnotatedFields.class,
                null).size(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldsUpToNullStartClass() {
        assertEquals(getFieldsUpTo(null, null).size(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldNoSuchField() {
        getField(getClass(), "test");
    }

    private static class ClassWithNoAnnotatedFields {
        private String stringValue;
        private Integer integer;
    }
}
