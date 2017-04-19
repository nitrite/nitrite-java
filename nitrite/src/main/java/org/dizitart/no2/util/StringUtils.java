package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;

/**
 * A utility class for {@link String}.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class StringUtils {
    /**
     * Checks if a string is `null` or empty string.
     *
     * @param value the string value
     * @return `true` if `null` or empty string.
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || "".equalsIgnoreCase(value);
    }

    /**
     * Checks if a {@link CharSequence} is `null` or empty.
     *
     * @param value the {@link CharSequence}
     * @return `true` if `null` or empty.
     */
    public static boolean isNullOrEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }
}
