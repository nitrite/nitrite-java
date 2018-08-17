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

package org.dizitart.no2.mapper;

import org.dizitart.no2.Document;

import static org.dizitart.no2.util.ObjectUtils.newInstance;

/**
 * This class provides a skeletal implementation of a {@link NitriteMapper}.
 * If an object implements a {@link Mappable} interface, it will use that
 * implementation to generate a {@link Document}, otherwise it will use
 * vendor specific serializer implementation.
 *
 * @author Anindya Chatterjee
 * @since 2.0
 */
public abstract class AbstractMapper implements NitriteMapper {

    /**
     * A child class must override this method for vendor specific
     * serialization.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the document
     */
    protected abstract <T> Document asDocumentAuto(T object);


    /**
     * A child class must override this method for vendor specific
     * de-serialization.
     *
     * @param <T>      the type parameter
     * @param document the document
     * @param type     the type
     * @return the de-serialized object
     */
    protected abstract <T> T asObjectAuto(Document document, Class<T> type);

    @Override
    public <T> Document asDocument(T object) {
        if (object instanceof Mappable) {
            Mappable mappable = (Mappable) object;
            return mappable.write(this);
        }
        return asDocumentAuto(object);
    }

    @Override
    public <T> T asObject(Document document, Class<T> type) {
        if (Mappable.class.isAssignableFrom(type)) {
            T item = newInstance(type);
            ((Mappable) item).read(this, document);
            return item;
        }
        return asObjectAuto(document, type);
    }
}
