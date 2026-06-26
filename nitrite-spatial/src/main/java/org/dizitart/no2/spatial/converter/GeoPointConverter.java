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

package org.dizitart.no2.spatial.converter;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.spatial.GeoPoint;

/**
 * Converter for {@link GeoPoint} to/from Nitrite {@link Document}.
 * 
 * <p>Stores GeoPoint as a document with latitude and longitude fields.</p>
 * 
 * @since 4.3.3
 * @author Anindya Chatterjee
 */
public class GeoPointConverter implements EntityConverter<GeoPoint> {
    
    @Override
    public Class<GeoPoint> getEntityType() {
        return GeoPoint.class;
    }

    @Override
    public Document toDocument(GeoPoint entity, NitriteMapper nitriteMapper) {
        return Document.createDocument("latitude", entity.getLatitude())
                       .put("longitude", entity.getLongitude());
    }

    @Override
    public GeoPoint fromDocument(Document document, NitriteMapper nitriteMapper) {
        Double latitude = document.get("latitude", Double.class);
        Double longitude = document.get("longitude", Double.class);
        
        if (latitude == null || longitude == null) {
            return null;
        }
        
        return new GeoPoint(latitude, longitude);
    }
}
