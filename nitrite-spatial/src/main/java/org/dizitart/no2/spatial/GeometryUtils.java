/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

import org.dizitart.no2.exceptions.NitriteIOException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeometryUtils {
    private static final String GEOMETRY_ID = "geometry:";
    private static WKTWriter writer;
    private static WKTReader reader;

    static {
        writer = new WKTWriter();
        reader = new WKTReader();
    }

    private GeometryUtils() {
    }

    public static String toString(Geometry geometry) {
        return GEOMETRY_ID + writer.write(geometry);
    }

    public static Geometry fromString(String geometryValue) {
        try {
            if (geometryValue.contains(GEOMETRY_ID)) {
                String geometry = geometryValue.replace(GEOMETRY_ID, "");
                return reader.read(geometry);
            } else {
                throw new NitriteIOException("Not a valid WKT geometry string " + geometryValue);
            }
        } catch (ParseException pe) {
            throw new NitriteIOException("Failed to parse WKT geometry string", pe);
        }
    }
}
