/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.util;

import org.dizitart.no2.index.IndexDescriptor;

import static org.dizitart.no2.common.Constants.*;

/**
 * A utility class for index.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class IndexUtils {
    private IndexUtils() {}

    /**
     * Derives index map name.
     *
     * @param descriptor the descriptor
     * @return the string
     */
    public static String deriveIndexMapName(IndexDescriptor descriptor) {
        return INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getCollectionName() +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getIndexFields().getEncodedName() +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getIndexType();
    }

    /**
     * Derives index meta map name.
     *
     * @param collectionName the collection name
     * @return the string
     */
    public static String deriveIndexMetaMapName(String collectionName) {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + collectionName;
    }
}
