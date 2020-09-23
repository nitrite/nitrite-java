package org.dizitart.no2.common.util;

/**
 * @author Anindya Chatterjee
 */
public class Comparables {
    private Comparables() {}

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
