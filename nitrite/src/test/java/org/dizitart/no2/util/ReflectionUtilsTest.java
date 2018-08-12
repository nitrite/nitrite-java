/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.util;

import org.junit.Test;

import static org.dizitart.no2.util.ReflectionUtils.getAnnotatedFields;
import static org.dizitart.no2.util.ReflectionUtils.getFieldsUpto;
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
        assertEquals(getFieldsUpto(A.class, B.class).size(), 3);
        assertEquals(getFieldsUpto(A.class, Object.class).size(), 5);
        assertEquals(getFieldsUpto(A.class, null).size(), 5);

        assertEquals(getFieldsUpto(ClassWithAnnotatedFields.class,
                Object.class).size(), 5);
        assertEquals(getFieldsUpto(ClassWithAnnotatedFields.class,
                null).size(), 5);
        assertEquals(getFieldsUpto(ClassWithAnnotatedFields.class,
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
