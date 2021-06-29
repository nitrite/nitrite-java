package org.dizitart.no2.common.util;

/**
 * A utility class for comparables.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class Comparables {
    private Comparables() {}

    /**
     * Compares two comparable objects.
     *
     * @param first  the first
     * @param second the second
     * @return the int
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int compare(Comparable first, Comparable second) {
        if (first instanceof Number && second instanceof Number) {
            Number number1 = (Number) first;
            Number number2 = (Number) second;
            int result = Numbers.compare(number1, number2);
            if (!first.getClass().equals(second.getClass())) {
                if (result == 0) return 1;
            }
            return result;
        }

        return first.compareTo(second);
    }
}
