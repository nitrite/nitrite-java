/*
 * Copyright (c) 2017-2024 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.dizitart.no2.spatial;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

/** Extends the JTS GeometricShapeFactory to add geodetic "small circle" geometry. */
public class GeometricShapeFactoryExt extends GeometricShapeFactory {

    /**
     * Bitmask specifying which results should be returned from the "direct" calculation.
     * We don't need to know the azimuth at s2, so we can save a couple of CPU cycles by not asking for it!
     * <p/>
     * See javadoc at {@link Geodesic#Direct(double, double, double, double, int)} for more details.
     */
    public static final int OUTMASK = GeodesicMask.STANDARD ^ GeodesicMask.AZIMUTH;

    @Override
    public Polygon createCircle()
    {
        Envelope env = dim.getEnvelope();
        double radiusInMeters = env.getWidth() / 2.0;
        double ctrX = dim.getCentre().x;
        double ctrY = dim.getCentre().y;

        Coordinate[] pts = new Coordinate[nPts + 1];
        int i;
        for (i = 0; i < nPts; i++) {
            // because this is the "azimuth" value, it starts at "geodetic north" and proceeds clockwise
            double azimuthInDegrees = i * (360.0 / nPts);
            GeodesicData directResult = Geodesic.WGS84.Direct(ctrX, ctrY, azimuthInDegrees, radiusInMeters, OUTMASK);
            double lat = directResult.lat2;
            double lon = directResult.lon2;
            pts[i] = coord(lat, lon);
        }
        pts[i] = new Coordinate(pts[0]);

        LinearRing ring = geomFact.createLinearRing(pts);
        return geomFact.createPolygon(ring);
    }
}
