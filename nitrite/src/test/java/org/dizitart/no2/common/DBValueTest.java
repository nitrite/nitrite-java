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

package org.dizitart.no2.common;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DBValueTest {

    @Test
    public void testSmallNumbersAreNormalizedToDouble() {
        // cross-type equality for values a double holds exactly, including in stores that
        // compare the encoded key rather than going through compareTo
        assertEquals(new DBValue(5.0), new DBValue(5));
        assertEquals(new DBValue(5.0), new DBValue(5L));
        assertEquals(new DBValue(5.0), new DBValue((short) 5));
        assertEquals(new DBValue(5.0), new DBValue((byte) 5));
        assertEquals(new DBValue(5.0), new DBValue(BigInteger.valueOf(5)));
    }

    @Test
    public void testLargeLongsKeepTheirValue() {
        long id = 870000000000000123L;   // beyond 2^53, doubles are 128 apart here
        assertEquals(id, new DBValue(id).getValue());
    }

    @Test
    public void testLongsCloserThanDoublePrecisionStayDistinct() {
        long id = 870000000000000123L;
        assertNotEquals(new DBValue(id), new DBValue(id + 1));
        assertNotEquals(0, new DBValue(id).compareTo(new DBValue(id + 1)));
    }

    @Test
    public void testLargeBigIntegerKeepsItsValue() {
        // odd and far beyond 2^53, so no double holds it exactly
        BigInteger value = BigInteger.ONE.shiftLeft(70).add(BigInteger.ONE);
        assertEquals(value, new DBValue(value).getValue());
        assertNotEquals(new DBValue(value), new DBValue(value.add(BigInteger.valueOf(2))));
    }

    @Test
    public void testLongAtTheEdgeOfExactRange() {
        long exact = 1L << 53;          // the largest power of two a double still steps by one
        assertEquals(2.0 * (1L << 52), new DBValue(exact).getValue());
        // one above it is not representable, so it has to keep its own value
        assertEquals(exact + 1, new DBValue(exact + 1).getValue());
    }

    @Test
    public void testExactlyRepresentableLargeValuesStillNormalize() {
        // 2^63 is a power of two, so the conversion loses nothing and folding is safe
        BigInteger powerOfTwo = BigInteger.ONE.shiftLeft(63);
        assertEquals(Math.pow(2, 63), new DBValue(powerOfTwo).getValue());
    }

    @Test
    public void testNumbersStillCompareAcrossTypes() {
        // compareTo goes through Comparables/Numbers, so this holds whatever the stored form is
        long id = 870000000000000123L;
        assertEquals(0, new DBValue(id).compareTo(new DBValue(BigInteger.valueOf(id))));
        assertEquals(0, new DBValue(5).compareTo(new DBValue(5.0)));
    }
}
