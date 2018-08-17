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
import org.dizitart.no2.index.Index;

import java.util.*;

import static org.dizitart.no2.common.Constants.INDEX_PREFIX;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.exceptions.ErrorCodes.VE_INDEX_NULL_INDEX;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * A utility class for nitrite {@link Index}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class IndexUtils {
    /**
     * Gets the internal name of an {@link Index}.
     *
     * @param index the index
     * @return the internal name.
     */
    public static String internalName(Index index) {
        notNull(index, errorMessage("index can not be null", VE_INDEX_NULL_INDEX));

        return  INDEX_PREFIX +
                INTERNAL_NAME_SEPARATOR +
                index.getCollectionName() +
                INTERNAL_NAME_SEPARATOR +
                index.getField() +
                INTERNAL_NAME_SEPARATOR +
                index.getIndexType();
    }

    /**
     * Sorts a map against it values. It is used to sort a score map during
     * full-text index search.
     *
     * @param <K>         the key type
     * @param <V>         the value type
     * @param unsortedMap the unsorted map
     * @return sorted map based on values.
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByScore(Map<K, V> unsortedMap) {
        List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (e1, e2) -> (e2.getValue()).compareTo(e1.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
