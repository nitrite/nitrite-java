package org.dizitart.no2.common.util;

/**
 * A utility class for comparables.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class Comparables {
    private Comparables() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int compare(Comparable first, Comparable second) {
        if (first instanceof Number && second instanceof Number) {
            Number number1 = (Number) first;
            Number number2 = (Number) second;
            return Numbers.compare(number1, number2);
        }

        return first.compareTo(second);
    }
}
