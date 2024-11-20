package org.locationtech.jts.util;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

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
