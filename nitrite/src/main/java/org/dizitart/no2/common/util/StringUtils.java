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

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * A utility class for {@link String}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class StringUtils {
    private StringUtils() {}

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

    /**
     * Returns a new String composed of copies of the `strings`
     * joined together with a copy of the specified `separator`.
     *
     * @param separator the delimiter that separates each element
     * @param strings   the elements to join together.
     * @return a new {@code String} that is composed of the `strings`
     * separated by the `separator`
     * @since 4.0.0
     */
    public static String join(String separator, String[] strings) {
        return join(separator, Arrays.asList(strings));
    }

    public static String join(String separator, Iterable<String> strings) {
        StringBuilder sb = new StringBuilder();
        int end = 0;
        for (String s : strings) {
            if (s != null) {
                sb.append(s);
                end = sb.length();
                sb.append(separator);
            }
        }
        return sb.substring(0, end);
    }

    public static StringTokenizer stringTokenizer(String text) {
        String delimiters = " \t\n\r\f+\"*%&/()=?'!,.;:-_#@|^~`{}[]<>\\";
        return new StringTokenizer(text, delimiters);
    }
}
