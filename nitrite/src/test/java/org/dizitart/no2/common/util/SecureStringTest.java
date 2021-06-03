/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SecureStringTest {
    @Test
    public void testConstructor() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new SecureString(1, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String()))))));
        assertEquals(2, (new SecureString(1, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String("foo"))))))).length());
        assertEquals(2, (new SecureString(1, 3, new String("foo"))).length());
        assertThrows(NegativeArraySizeException.class, () -> new SecureString(Integer.MIN_VALUE, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String()))))));
        assertEquals(0,
            (new SecureString(new SecureString(new SecureString(new SecureString(new SecureString(new String()))))))
                .length());
        assertEquals(1,
            (new SecureString(new SecureString(new SecureString(new SecureString(new SecureString(String.valueOf('A')))))))
                .length());
        assertEquals(0, (new SecureString(new String())).length());
        assertEquals(1, (new SecureString(String.valueOf('A'))).length());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new SecureString(1, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String()))))));
        assertThrows(ArrayIndexOutOfBoundsException.class,
            () -> new SecureString(1, 3, new SecureString(new SecureString(new SecureString(Long.toString(1L))))));
        assertEquals(2, (new SecureString(1, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String("foo"))))))).length());
        assertEquals(2, (new SecureString(1, 3, new String("foo"))).length());
        assertThrows(NegativeArraySizeException.class, () -> new SecureString(Integer.MIN_VALUE, 3,
            new SecureString(new SecureString(new SecureString(new SecureString(new String()))))));
        assertEquals(0,
            (new SecureString(new SecureString(new SecureString(new SecureString(new SecureString(new String()))))))
                .length());
        assertEquals(1,
            (new SecureString(new SecureString(new SecureString(new SecureString(new SecureString(String.valueOf('A')))))))
                .length());
        assertEquals(0, (new SecureString(new String())).length());
        assertEquals(1, (new SecureString(String.valueOf('A'))).length());
    }

    @Test
    public void testCharAt2() {
        assertEquals('o',
            (new SecureString(new SecureString(new SecureString(new SecureString(new String("foo")))))).charAt(1));
    }

    @Test
    public void testCharAt4() {
        assertEquals('o',
            (new SecureString(new SecureString(new SecureString(new SecureString(new String("foo")))))).charAt(1));
    }

    @Test
    public void testLength() {
        assertEquals(0, (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).length());
        assertEquals(0, (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).length());
    }


    @Test
    public void testSubSequence2() {
        assertEquals(2,
            (new SecureString(new SecureString(new SecureString(new SecureString(new String("foo")))))).subSequence(1, 3)
                .length());
    }

    @Test
    public void testSubSequence3() {
        assertThrows(NegativeArraySizeException.class,
            () -> (new SecureString(new SecureString(new SecureString(new SecureString(new String())))))
                .subSequence(Integer.MIN_VALUE, 3));
    }


    @Test
    public void testSubSequence5() {
        assertEquals(2,
            (new SecureString(new SecureString(new SecureString(new SecureString(new String("foo")))))).subSequence(1, 3)
                .length());
    }

    @Test
    public void testSubSequence6() {
        assertThrows(NegativeArraySizeException.class,
            () -> (new SecureString(new SecureString(new SecureString(new SecureString(new String())))))
                .subSequence(Integer.MIN_VALUE, 3));
    }

    @Test
    public void testAsString() {
        assertEquals("", (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).asString());
        assertEquals("A",
            (new SecureString(new SecureString(new SecureString(new SecureString(String.valueOf('A')))))).asString());
        assertEquals("", (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).asString());
        assertEquals("A",
            (new SecureString(new SecureString(new SecureString(new SecureString(String.valueOf('A')))))).asString());
    }

    @Test
    public void testToString() {
        assertEquals("Secure:XXXXX",
            (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).toString());
        assertEquals("Secure:XXXXX",
            (new SecureString(new SecureString(new SecureString(new SecureString(new String()))))).toString());
    }
}

