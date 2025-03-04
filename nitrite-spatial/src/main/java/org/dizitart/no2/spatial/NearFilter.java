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

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.FlattenableFilter;
import org.dizitart.no2.filters.FieldBasedFilter;
import org.dizitart.no2.filters.Filter;
import org.locationtech.jts.geom.*;

import java.util.List;

import static org.locationtech.jts.geom.PrecisionModel.FLOATING;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class NearFilter extends IntersectsFilter implements FlattenableFilter {
    private Point center;
    private Double distance;

    /** Uses full "double" floating-point precision, and <a href="https://epsg.io/4326">SRID 4326</a> */
    private static GeometryFactory geometryFactory =
        new GeometryFactory(new PrecisionModel(FLOATING), 4326);


    NearFilter(String field, Coordinate center, Double distance) {
        super(field, createCircle(center, distance));
        this.center = geometryFactory.createPoint(center);
        this.distance = distance;
    }

    NearFilter(String field, Point center, Double distance) {
        super(field, createCircle(center.getCoordinate(), distance));
        this.center = center;
        this.distance = distance;
    }

    private static Geometry createCircle(Coordinate center, double radius) {
        GeometricShapeFactoryExt shapeFactory = new GeometricShapeFactoryExt();
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(center);
        shapeFactory.setSize(radius * 2);
        return shapeFactory.createCircle();
    }

    @Override
    public String toString() {
        return "(" + getField() + " nears " + getValue() + ")";
    }

    @Override
    public List<Filter> getFilters() {
        return List.of(
            // [PR note] Use of "IntersectsFilter" was an arbitrary choice. Any of the misbehaving filters that
            //   are really just doing a bounding box test within the spatial index would have worked.
            //   The important thing for now was to not accidentally produce *recursive* flattening, and at the
            //   time I wrote this line, I was still planning to edit WithinFilter to have it also implement
            //   the FlattenableFilter interface
            new IntersectsFilter(getField(), getValue()),
            new NonIndexNearFilter(getField(), getValue()));
    }

    // [PR note] This is probably the first time in years I've used a non-static inner class. I think we
    //    should prefer to avoid the pattern in the final code, but it saved some boiler-plate here for the
    //    proof-of-concept.
    public class NonIndexNearFilter extends FieldBasedFilter {

        protected NonIndexNearFilter(String field, Geometry circle) {
            super(field, circle);
        }

        @Override
        public boolean apply(Pair<NitriteId, Document> element) {
            Document document = element.getSecond();
            Object fieldValue = document.get(getField());

            if (fieldValue == null) {
                return false;
            } else if (fieldValue instanceof Geometry) {
                if (fieldValue instanceof Point) {
                    Point pointValue = (Point) fieldValue;
                    Point centerPoint = NearFilter.this.center;
                    GeodesicData inverseResult =
                        Geodesic.WGS84.Inverse(
                            centerPoint.getX(), centerPoint.getY(),
                            pointValue.getX(), pointValue.getY(),
                            GeodesicMask.DISTANCE);
                    return inverseResult.s12 <= NearFilter.this.distance;
            } else {
                    // TODO this doesn't seem to work??
                    Geometry elemGeo = (Geometry) fieldValue;
                    Geometry filterGeo = (Geometry) getValue();
                    return filterGeo.intersects(elemGeo);
                }
            } else {
                throw new FilterException(getField() + " does not contain Geometry value");
            }
        }

    }

}
