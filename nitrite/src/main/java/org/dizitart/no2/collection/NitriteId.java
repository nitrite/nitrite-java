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
import org.dizitart.no2.exceptions.InvalidIdException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.dizitart.no2.common.Constants.ID_PREFIX;
import static org.dizitart.no2.common.Constants.ID_SUFFIX;

/**
 * An unique identifier across the Nitrite database. Each document in
 * a nitrite collection is associated with a {@link NitriteId}.
 * <p>
 * During insertion if an unique object is supplied in the '_id' field
 * of the document, then the value of the '_id' field will be used to
 * create a new {@link NitriteId}. If that is not supplied, then nitrite
 * will auto generate one and supply it in the '_id' field of the document.
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#getById(NitriteId)
 * @since 1.0
 */
@EqualsAndHashCode
public final class NitriteId implements Comparable<NitriteId>, Serializable {
    private static final long serialVersionUID = 1477462375L;
    private transient static final SnowflakeIdGenerator generator = new SnowflakeIdGenerator();

    private String idValue;

    private NitriteId() {
        this.idValue = Long.toString(generator.getId());
    }

    private NitriteId(String value) {
        this.idValue = value;
    }

    /**
     * Gets a new auto-generated {@link NitriteId}.
     *
     * @return a new auto-generated {@link NitriteId}.
     */
    public static NitriteId newId() {
        return new NitriteId();
    }

    /**
     * Creates a {@link NitriteId} from a long value.
     *
     * @param value the value
     * @return the {@link NitriteId}
     */
    public static NitriteId createId(String value) {
        validId(value);
        return new NitriteId(value);
    }

    public static boolean validId(Object value) {
        if (value == null) {
            throw new InvalidIdException("id cannot be null");
        }
        try {
            Long.parseLong(value.toString());
            return true;
        } catch (Exception e) {
            throw new InvalidIdException("id must be a string representation of 64bit decimal number");
        }
    }

    @Override
    public int compareTo(NitriteId other) {
        if (other.idValue == null) {
            throw new InvalidIdException("cannot compare with null id");
        }

        return Long.compare(Long.parseLong(idValue), Long.parseLong(other.idValue));
    }

    @Override
    public String toString() {
        if (idValue != null) {
            return ID_PREFIX + idValue + ID_SUFFIX;
        }
        return "";
    }

    /**
     * Gets the underlying id object.
     *
     * @return the underlying id object.
     */
    public String getIdValue() {
        return idValue;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(idValue);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        idValue = stream.readUTF();
    }
}
