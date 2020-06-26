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

package org.dizitart.no2.store.compat.v3;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * A specialized version of {@link ObjectInputStream} for nitrite.
 *
 * @since 4.0.0
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteObjectInputStream extends ObjectInputStream {
    private static final Map<String, Class<?>> migrationMap = new HashMap<>();

    static {
        migrationMap.put("org.dizitart.no2.Security$UserCredential", Compat.UserCredential.class);
        migrationMap.put("org.dizitart.no2.NitriteId", Compat.NitriteId.class);
        migrationMap.put("org.dizitart.no2.Index", Compat.Index.class);
        migrationMap.put("org.dizitart.no2.IndexType", Compat.IndexType.class);
        migrationMap.put("org.dizitart.no2.internals.IndexMetaService$IndexMeta", Compat.IndexMeta.class);
        migrationMap.put("org.dizitart.no2.Document", Compat.Document.class);
        migrationMap.put("org.dizitart.no2.meta.Attributes", Compat.Attributes.class);
    }

    /**
     * Instantiates a new Nitrite object input stream.
     *
     * @param stream the stream
     * @throws IOException the io exception
     */
    public NitriteObjectInputStream(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

        for (final String oldName : migrationMap.keySet()) {
            if (resultClassDescriptor != null && resultClassDescriptor.getName().equals(oldName)) {
                Class<?> replacement = migrationMap.get(oldName);

                try {
                    resultClassDescriptor = ObjectStreamClass.lookup(replacement);
                } catch (Exception e) {
                    log.error("Error while replacing class name." + e.getMessage(), e);
                }
            }
        }

        return resultClassDescriptor;
    }
}
