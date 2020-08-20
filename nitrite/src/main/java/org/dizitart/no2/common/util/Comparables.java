package org.dizitart.no2.common.util;

/**
 * @author Anindya Chatterjee
 */
public class Comparables {
    private Comparables() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int compare(Comparable first, Comparable second) {
        if (first instanceof Number && second instanceof Number) {
            return Numbers.compare((Number) first, (Number) second);
        }

        return first.compareTo(second);
    }
}
