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

package org.dizitart.no2.spatial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.locationtech.jts.geom.Geometry;

import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Index(fields = "geometry", type = SPATIAL_INDEX)
public class SpatialData {
    @Id
    private Long id;
    private Geometry geometry;

    public static class SpatialDataConverter implements EntityConverter<SpatialData> {

        @Override
        public Class<SpatialData> getEntityType() {
            return SpatialData.class;
        }

        @Override
        public Document toDocument(SpatialData entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("id", entity.getId())
                .put("geometry", nitriteMapper.tryConvert(entity.getGeometry(), Document.class));
        }

        @Override
        public SpatialData fromDocument(Document document, NitriteMapper nitriteMapper) {
            SpatialData data = new SpatialData();
            data.setId(document.get("id", Long.class));
            data.setGeometry((Geometry) nitriteMapper.tryConvert(document.get("geometry"), Geometry.class));
            return data;
        }
    }
}
