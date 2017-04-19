package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.dizitart.no2.exceptions.ErrorCodes.VE_BIG_DECIMAL_INVALID_FORMAT;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * A utility class for {@link Number}s.
 *
 * @since 1.0
 * @author Anindya Chatterjee
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
        if(isSpecial(x) || isSpecial(y))
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
        if(number instanceof BigDecimal)
            return (BigDecimal) number;
        if(number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);
        if(number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long)
            return new BigDecimal(number.longValue());
        if(number instanceof Float || number instanceof Double)
            return new BigDecimal(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch(NumberFormatException e) {
            throw new ValidationException(errorMessage("The given number (\"" + number + "\" of class "
                    + number.getClass().getName() + ") does not have a parsable string representation",
                    VE_BIG_DECIMAL_INVALID_FORMAT), e);
        }
    }
}
