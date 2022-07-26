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

package org.dizitart.no2.index;

import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.exceptions.ValidationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dizitart.no2.common.util.ValidationUtils.validateArrayIndexField;
import static org.dizitart.no2.common.util.ValidationUtils.validateIterableIndexField;

/**
 * Represents a nitrite index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface NitriteIndex {
    /**
     * Gets index descriptor.
     *
     * @return the index descriptor
     */
    IndexDescriptor getIndexDescriptor();

    /**
     * Writes a {@link FieldValues} in the index.
     *
     * @param fieldValues the field values
     */
    void write(FieldValues fieldValues);

    /**
     * Removes a {@link FieldValues} from the index.
     *
     * @param fieldValues the field values
     */
    void remove(FieldValues fieldValues);

    /**
     * Drops this index.
     */
    void drop();

    /**
     * Finds a set of {@link NitriteId}s from the index after executing the {@link FindPlan}.
     *
     * @param findPlan the find plan
     * @return the linked hash set
     */
    LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan);

    /**
     * Indicates if this is an unique index.
     *
     * @return the boolean
     */
    default boolean isUnique() {
        return getIndexDescriptor().getIndexType().equalsIgnoreCase(IndexType.UNIQUE);
    }

    /**
     * Validates the index field.
     *
     * @param value the value
     * @param field the field
     */
    default void validateIndexField(Object value, String field) {
        if (value == null) return;
        if (value instanceof Iterable) {
            validateIterableIndexField((Iterable<?>) value, field);
        } else if (value.getClass().isArray()) {
            validateArrayIndexField(value, field);
        } else if (!(value instanceof Comparable)) {
            throw new ValidationException("Index field " + field + " must be a comparable type");
        }
    }

    /**
     * Adds a {@link NitriteId} of the {@link FieldValues} to the existing indexed list of {@link NitriteId}s.
     *
     * @param nitriteIds  the nitrite ids
     * @param fieldValues the field values
     * @return the list
     */
    default List<NitriteId> addNitriteIds(List<NitriteId> nitriteIds, FieldValues fieldValues) {
        if (nitriteIds == null) {
            nitriteIds = new CopyOnWriteArrayList<>();
        }

        if (isUnique() && nitriteIds.size() == 1) {
            // if key is already exists for unique type, throw error
            throw new UniqueConstraintException("Unique key constraint violation for " + fieldValues.getFields());
        }

        // index always are in ascending format
        nitriteIds.add(fieldValues.getNitriteId());
        return nitriteIds;
    }

    /**
     * Removes a {@link NitriteId} of the {@link FieldValues} from the existing indexed list of {@link NitriteId}s.
     *
     * @param nitriteIds  the nitrite ids
     * @param fieldValues the field values
     * @return the list
     */
    default List<NitriteId> removeNitriteIds(List<NitriteId> nitriteIds, FieldValues fieldValues) {
        if (nitriteIds != null && !nitriteIds.isEmpty()) {
            nitriteIds.remove(fieldValues.getNitriteId());
        }
        return nitriteIds;
    }
}
