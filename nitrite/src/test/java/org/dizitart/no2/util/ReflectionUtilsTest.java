package org.dizitart.no2.util;

import org.junit.Test;

import static org.dizitart.no2.util.ReflectionUtils.getAnnotatedFields;
import static org.dizitart.no2.util.ReflectionUtils.getFieldsUpTo;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ReflectionUtilsTest {

    @Test
    public void testGetAnnotatedFields() {
        assertEquals(getAnnotatedFields(ClassWithAnnotatedFields.class,
                Deprecated.class).size(), 2);
        assertEquals(getAnnotatedFields(ClassWithAnnotatedFields.class,
                Test.class).size(), 0);

        assertEquals(getAnnotatedFields(ClassWithNoAnnotatedFields.class,
                Deprecated.class).size(), 0);
    }

    @Test
    public void testGetFieldsUpTo() {
        assertEquals(getFieldsUpTo(A.class, B.class).size(), 3);
        assertEquals(getFieldsUpTo(A.class, Object.class).size(), 5);
        assertEquals(getFieldsUpTo(A.class, null).size(), 5);

        assertEquals(getFieldsUpTo(ClassWithAnnotatedFields.class,
                Object.class).size(), 5);
        assertEquals(getFieldsUpTo(ClassWithAnnotatedFields.class,
                null).size(), 5);
        assertEquals(getFieldsUpTo(ClassWithAnnotatedFields.class,
                ClassWithNoAnnotatedFields.class).size(), 3);
    }


    private static class ClassWithAnnotatedFields extends ClassWithNoAnnotatedFields {
        private String stringValue;

        @Deprecated
        private String anotherValue;

        @Deprecated
        Long longValue;

        @Deprecated
        public ClassWithAnnotatedFields() {
        }
    }

    private static class ClassWithNoAnnotatedFields {
        private String stringValue;
        private Integer integer;
    }

    private static class A extends B {
        private String a;
        private Long b;
        private Integer c;
    }

    private static class B {
        String a;
        private Short d;
    }
}
