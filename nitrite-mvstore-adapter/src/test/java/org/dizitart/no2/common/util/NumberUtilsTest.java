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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.dizitart.no2.common.util.NumberUtils.compare;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
@RunWith(Parameterized.class)
public class NumberUtilsTest {

    @Parameterized.Parameter
    public Number x;

    @Parameterized.Parameter(value = 1)
    public Number y;

    @Parameterized.Parameter(value = 2)
    public int result;

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
        });
    }

    @Test
    public void testCompare() {
        assertEquals(compare(x, y), result);
    }
}
