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

import org.dizitart.no2.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Numbers {
    private Numbers() {
    }

    public static int compare(Number x, Number y) {
        if (isSpecial(x) || isSpecial(y)) {
            return Double.compare(x.doubleValue(), y.doubleValue());
        }

        // Every index key comparison lands here, and going through BigDecimal allocates two
        // objects per call. The common cases can be answered exactly without it: any two
        // integral primitives fit a long, two doubles or two floats compare as themselves
        // once NaN and the infinities are out of the way (with -0.0 and 0.0 equal, as
        // BigDecimal treats them), and two BigDecimals or two BigIntegers already have a
        // compareTo. Everything else, and every mixed pairing, keeps the exact conversion.
        if (isIntegral(x) && isIntegral(y)) {
            return Long.compare(x.longValue(), y.longValue());
        }
        if (x instanceof Double && y instanceof Double) {
            return compareFinite(x.doubleValue(), y.doubleValue());
        }
        if (x instanceof Float && y instanceof Float) {
            return compareFinite(x.floatValue(), y.floatValue());
        }
        if (x instanceof BigDecimal && y instanceof BigDecimal) {
            return ((BigDecimal) x).compareTo((BigDecimal) y);
        }
        if (x instanceof BigInteger && y instanceof BigInteger) {
            return ((BigInteger) x).compareTo((BigInteger) y);
        }
        return toBigDecimal(x).compareTo(toBigDecimal(y));
    }

    private static boolean isIntegral(Number number) {
        return number instanceof Long || number instanceof Integer
            || number instanceof Short || number instanceof Byte;
    }

    private static int compareFinite(double a, double b) {
        return a < b ? -1 : (a > b ? 1 : 0);
    }

    private static boolean isSpecial(Number number) {
        boolean specialDouble = number instanceof Double
            && (Double.isNaN((Double) number) || Double.isInfinite((Double) number));
        boolean specialFloat = number instanceof Float
            && (Float.isNaN((Float) number) || Float.isInfinite((Float) number));
        return specialDouble || specialFloat;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal)
            return (BigDecimal) number;
        if (number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);
        if (number instanceof Byte || number instanceof Short
            || number instanceof Integer || number instanceof Long)
            return new BigDecimal(number.longValue());
        if (number instanceof Float || number instanceof Double)
            return BigDecimal.valueOf(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch (NumberFormatException e) {
            throw new ValidationException("The given number (\"" + number + "\" of class "
                + number.getClass().getName() + ") does not have a parsable string representation", e);
        }
    }
}
