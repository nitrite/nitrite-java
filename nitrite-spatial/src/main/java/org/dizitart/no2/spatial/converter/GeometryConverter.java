package org.dizitart.no2.spatial.converter;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.spatial.GeometryUtils;
import org.locationtech.jts.geom.Geometry;

public class GeometryConverter implements EntityConverter<Geometry> {
    @Override
    public Class<Geometry> getEntityType() {
        return Geometry.class;
    }

    @Override
    public Document toDocument(Geometry entity, NitriteMapper nitriteMapper) {
        return Document.createDocument("geometry", GeometryUtils.toString(entity));
    }

    @Override
    public Geometry fromDocument(Document document, NitriteMapper nitriteMapper) {
        String value = document.get("geometry", String.class);
        return GeometryUtils.fromString(value);
    }
}
