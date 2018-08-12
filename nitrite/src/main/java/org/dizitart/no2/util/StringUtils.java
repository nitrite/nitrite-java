/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
