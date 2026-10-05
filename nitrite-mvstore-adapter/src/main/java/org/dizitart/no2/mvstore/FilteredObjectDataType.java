/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mvstore;

import org.h2.mvstore.DataUtils;
import org.h2.mvstore.type.ObjectDataType;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import static org.h2.mvstore.DataUtils.ERROR_SERIALIZATION;

/**
 * An {@link ObjectDataType} that deserializes stored objects through a JEP 290
 * allowlist instead of H2's unfiltered {@code ObjectInputStream} (CWE-502, see
 * GHSA-7w7v-2j76-mqwp). The on-disk format is unchanged.
 *
 * @author Anindya Chatterjee
 * @since 5.3.1
 */
class FilteredObjectDataType extends ObjectDataType {
    // tags and common class ids of h2's ObjectDataType format (h2-mvstore 2.x)
    private static final int TYPE_ARRAY = 14;
    private static final int TYPE_SERIALIZED_OBJECT = 19;
    private static final int LAST_PRIMITIVE_CLASS_ID = 7;

    private static final String DEFAULT_ALLOWED = "org.dizitart.no2.**;java.**";

    private final ObjectInputFilter filter;

    FilteredObjectDataType(ObjectInputFilter filter) {
        this.filter = filter;
    }

    /**
     * Builds the filter allowing nitrite's own and JDK types, plus the given
     * JEP 290 patterns; a process-wide filter, if set, can still reject.
     */
    static ObjectInputFilter createFilter(String allowedClasses) {
        String pattern = DEFAULT_ALLOWED
            + (allowedClasses == null || allowedClasses.isEmpty() ? "" : ";" + allowedClasses) + ";!*";
        ObjectInputFilter own = ObjectInputFilter.Config.createFilter(pattern);
        ObjectInputFilter global = ObjectInputFilter.Config.getSerialFilter();
        if (global == null) {
            return own;
        }
        return info -> global.checkInput(info) == ObjectInputFilter.Status.REJECTED
            ? ObjectInputFilter.Status.REJECTED : own.checkInput(info);
    }

    @Override
    public Object read(ByteBuffer buff) {
        int tag = buff.get(buff.position());
        if (tag == TYPE_SERIALIZED_OBJECT) {
            buff.get();
            byte[] data = new byte[DataUtils.readVarInt(buff)];
            buff.get(data);
            return readFiltered(data);
        }
        if (tag == TYPE_ARRAY) {
            // h2 reads the elements of an object array with its own, unfiltered
            // ObjectDataType; nitrite never stores one, so refuse it
            int classId = buff.get(buff.position() + 1);
            if (classId < 0 || classId > LAST_PRIMITIVE_CLASS_ID) {
                throw DataUtils.newMVStoreException(ERROR_SERIALIZATION,
                    "Object arrays are not allowed in a nitrite map");
            }
        }
        return super.read(buff);
    }

    private Object readFiltered(byte[] data) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            in.setObjectInputFilter(filter);
            return in.readObject();
        } catch (Exception e) {
            throw DataUtils.newMVStoreException(ERROR_SERIALIZATION, "Could not deserialize {0}", e, e);
        }
    }
}
