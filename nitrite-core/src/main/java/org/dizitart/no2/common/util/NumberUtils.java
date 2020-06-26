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

import lombok.experimental.UtilityClass;
import org.dizitart.no2.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A utility class for {@link Number}s.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@UtilityClass
public class NumberUtils {
    /**
     * Compare two numbers.
     *
     * @param x first number.
     * @param y second number.
     * @return `0` if `x` and `y` are numerically equal. A value less
     * than `0` if `x` is numerically less than `y`. A value greater
     * than `0` if `x` is numerically greater than `y`.
     */
    public static int compare(Number x, Number y) {
        if (isSpecial(x) || isSpecial(y))
            return Double.compare(x.doubleValue(), y.doubleValue());
        else
            return toBigDecimal(x).compareTo(toBigDecimal(y));
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
            return new BigDecimal(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch (NumberFormatException e) {
            throw new ValidationException("the given number (\"" + number + "\" of class "
                + number.getClass().getName() + ") does not have a parsable string representation", e);
        }
    }
}
