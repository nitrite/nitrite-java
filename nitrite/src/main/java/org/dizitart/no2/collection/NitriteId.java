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

package org.dizitart.no2.collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.dizitart.no2.exceptions.InvalidIdException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.dizitart.no2.common.Constants.ID_PREFIX;
import static org.dizitart.no2.common.Constants.ID_SUFFIX;

/**
 * A unique identifier across the Nitrite database. Each document in
 * a nitrite collection is associated with a {@link NitriteId}.
 * <p>
 * During insertion if a unique object is supplied in the '_id' field
 * of the document, then the value of the '_id' field will be used to
 * create a new {@link NitriteId}. If the '_id' field is not supplied, then
 * nitrite will generate a new [NitriteId] and will add it to the document.
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#getById(NitriteId)
 * @since 1.0
 */
@Getter
@EqualsAndHashCode
public final class NitriteId implements Comparable<NitriteId>, Serializable {
    private static final long serialVersionUID = 1477462375L;
    private static final SnowflakeIdGenerator generator = new SnowflakeIdGenerator();

    /** The underlying value of the NitriteId. */
    /* WARNING(gh-1162): naming it idValue breaks java serialization for databases created with earlier version of nitrite
     *                   and marking it transient introduces side effects for @EqualsAndHashCode */
    private long _idValue;

    private NitriteId() {
        this._idValue = generator.getId();
    }

    private NitriteId(String value) {
        this._idValue = Long.parseLong(value);
    }

    private NitriteId(long value) {
        this._idValue = value;
    }

    /**
     * Creates a new auto-generated {@link NitriteId}.
     *
     * @return a new auto-generated {@link NitriteId}.
     */
    public static NitriteId newId() {
        return new NitriteId();
    }

    /**
     * Creates a {@link NitriteId} from a value.
     * <p>
     * The value must be a string representation of a 64bit integer number.
     *
     * @param value the value
     * @return the {@link NitriteId}
     */
    public static NitriteId createId(String value) {
        validId(value);
        return new NitriteId(value);
    }

    /**
     * Creates a {@link NitriteId} from a {@code long} value.
     *
     * @param value the value
     * @return the {@link NitriteId}
     */
    public static NitriteId createId(long value) {
        validId(value);
        return new NitriteId(value);
    }

    /**
     * Validates a value to be used as {@link NitriteId}.
     * <p>
     * The value must be a string representation of a 64bit integer number.
     *
     * @param value the value
     * @return `true` if the value is valid; otherwise `false`.
     * */
    public static boolean validId(Object value) {
        if (value == null) {
            throw new InvalidIdException("id cannot be null");
        }
        try {
            Long.parseLong(value.toString());
            return true;
        } catch (Exception e) {
            throw new InvalidIdException("id must be a string representation of 64bit integer number " + value);
        }
    }

    public long getIdValue() {
        return _idValue;
    }

    @Override
    public int compareTo(NitriteId other) {
        return Long.compare(_idValue, other._idValue);
    }

    @Override
    public String toString() {
        return ID_PREFIX + _idValue + ID_SUFFIX;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(Long.toString(_idValue));
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        _idValue = Long.parseLong(stream.readUTF());
    }
}
