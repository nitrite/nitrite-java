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

package org.dizitart.no2;

import lombok.EqualsAndHashCode;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.InvalidIdException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.common.Constants.ID_PREFIX;
import static org.dizitart.no2.common.Constants.ID_SUFFIX;
import static org.dizitart.no2.exceptions.ErrorMessage.CAN_NOT_COMPARE_WITH_NULL_ID;
import static org.dizitart.no2.exceptions.ErrorMessage.FAILED_TO_CREATE_AUTO_ID;

/**
 * An unique identifier across the Nitrite database. Each document in
 * a nitrite collection is associated with a {@link NitriteId}.
 *
 * During insertion if an unique object is supplied in the '_id' field
 * of the document, then the value of the '_id' field will be used to
 * create a new {@link NitriteId}. If that is not supplied, then nitrite
 * will auto generate one and supply it in the '_id' field of the document.
 *
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#getById(NitriteId)
 * @since 1.0
 */
@EqualsAndHashCode
public final class NitriteId implements Comparable<NitriteId>, Serializable {
    private static final long serialVersionUID = 1477462375L;
    private static final AtomicLong counter = new AtomicLong(System.nanoTime());

    private Long idValue;

    private NitriteId() {
        idValue = counter.getAndIncrement();
    }

    private NitriteId(Long value) {
        idValue = value;
    }

    /**
     * Gets a new auto-generated {@link NitriteId}.
     *
     * @return a new auto-generated {@link NitriteId}.
     */
    public static NitriteId newId() {
        try {
            return new NitriteId();
        } catch (Exception e) {
            throw new InvalidIdException(FAILED_TO_CREATE_AUTO_ID, e);
        }
    }

    /**
     * Creates a {@link NitriteId} from a long value.
     *
     * @param value the value
     * @return the {@link NitriteId}
     */
    public static NitriteId createId(Long value) {
        return new NitriteId(value);
    }

    @Override
    public int compareTo(@NotNull NitriteId other) {
        if (other.idValue == null) {
            throw new InvalidIdException(CAN_NOT_COMPARE_WITH_NULL_ID);
        }

        return Long.compare(idValue, other.idValue);
    }

    @Override
    public String toString() {
        if (idValue != null) {
            return ID_PREFIX + idValue.toString() + ID_SUFFIX;
        }
        return "";
    }

    /**
     * Gets the underlying id object.
     *
     * @return the underlying id object.
     */
    public Long getIdValue() {
        if (idValue != null) return idValue;
        return null;
    }
}
