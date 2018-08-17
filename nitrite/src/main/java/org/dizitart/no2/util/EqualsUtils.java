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

import java.lang.reflect.Array;
import java.util.Map;

import static org.dizitart.no2.util.Iterables.toArray;
import static org.dizitart.no2.util.NumberUtils.compare;

/**
 * A utility class to compute equality.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@UtilityClass
public class EqualsUtils {

    /**
     * Computes equality of two objects.
     *
     * @param o1 the first object
     * @param o2 the other object
     * @return `true` if two objects are equal.
     */
    public static boolean deepEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        }

        if (o1 == o2) {
            // if reference equal send true
            return true;
        }

        if (o1 instanceof Number && o2 instanceof Number) {
            // cast to Number and take care of boxing and compare
            return compare((Number) o1, (Number) o2) == 0;
        } else if (o1 instanceof Iterable && o2 instanceof Iterable)  {
            Object[] arr1 = toArray((Iterable) o1);
            Object[] arr2 = toArray((Iterable) o2);
            // convert iterable to array and recursively compare arrays
            return deepEquals(arr1, arr2);
        } else if (o1.getClass().isArray() && o2.getClass().isArray()) {
            // if both are object array iterate each element and recursively check
            // it respects cardinality of the elements in the array
            int length = Array.getLength(o1);

            if (length != Array.getLength(o2)) {
                return false;
            }

            for (int i = 0; i < length; i++) {
                Object item1 = Array.get(o1, i);
                Object item2 = Array.get(o2, i);

                if (!deepEquals(item1, item2)) {
                    // if one element is not equal return false
                    return false;
                }
            }
            // if all check passed it must be equal
            return true;
        } else if (o1 instanceof Map && o2 instanceof Map) {
            Map map1 = (Map) o1;
            Map map2 = (Map) o2;
            return deepEquals(toArray(map1.entrySet()), toArray(map2.entrySet()));
        } else {
            // generic check
            return o1.equals(o2);
        }

        // none of the type check passes so they are not of compatible type
    }
}
