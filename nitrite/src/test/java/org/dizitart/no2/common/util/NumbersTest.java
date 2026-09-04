/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.util;

import org.dizitart.no2.integration.Retry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.dizitart.no2.common.util.Numbers.compare;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
@RunWith(Parameterized.class)
public class NumbersTest {

    @Parameterized.Parameter
    public Number x;

    @Parameterized.Parameter(value = 1)
    public Number y;

    @Parameterized.Parameter(value = 2)
    public int result;

    @Rule
    public Retry retry = new Retry(3);

    @Parameterized.Parameters(name = "compare({0}, {1}) = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1, 1.0f, 0},
                {1.0f, 1, 0},
                {1, 1.00000000000001d, -1},
                {1.00000000000001d, 1, 1},
                {1, 1.00001f, -1},
                {1.00001f, 1, 1},
                {Double.NaN, Float.NaN, 0},
                {BigDecimal.ONE, 1, 0},
                {1, BigDecimal.ONE, 0},
                {BigInteger.ONE, BigDecimal.ONE, 0},
                {BigInteger.ONE, 1, 0},
                {0, BigInteger.ZERO, 0},
                {Integer.MAX_VALUE, Integer.MAX_VALUE, 0},
                {Double.MAX_VALUE, Float.MAX_VALUE, 1},
                {Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 0},
                {Double.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, -1},
                {Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0},
                // same-type fast paths must agree with the BigDecimal conversion
                {Long.MAX_VALUE, Long.MIN_VALUE, 1},
                {Long.MIN_VALUE, Long.MAX_VALUE, -1},
                {Long.MAX_VALUE, Long.MAX_VALUE - 1, 1},
                {(byte) 3, 3L, 0},
                {(short) -2, -1, -1},
                {Integer.MIN_VALUE, Long.MIN_VALUE, 1},
                {0.0, -0.0, 0},
                {-0.0, 0.0, 0},
                {1.5, 1.25, 1},
                {1.5f, 1.75f, -1},
                {Double.NaN, 1.0, 1},
                {1.0, Double.NaN, -1},
                {0.1f, 0.1, 1}, // 0.1f widens to 0.10000000149, above 0.1: mixed types keep the exact path
                {BigInteger.TEN, BigInteger.ONE, 1},
                {new BigDecimal("2.50"), new BigDecimal("2.5"), 0},
                {Long.MAX_VALUE, Double.MAX_VALUE, -1},
                {Long.MAX_VALUE, new BigInteger("9223372036854775808"), -1},
        });
    }

    @Test
    public void testCompare() {
        assertEquals(compare(x, y), result);
    }

    @Test
    public void testCompare2() {
        Integer x = 1;
        assertEquals(0, Numbers.compare(x, 1));
    }
}
