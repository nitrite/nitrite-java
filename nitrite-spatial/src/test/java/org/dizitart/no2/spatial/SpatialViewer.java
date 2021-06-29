/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anindya Chatterjee
 */
public class SpatialViewer {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().add(new Paint());
        f.setSize(700, 700);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static class Paint extends JPanel {

        public void paint(Graphics g) {
            try {
                Graphics2D g2d = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                rh.put(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

                g2d.setRenderingHints(rh);


                WKTReader reader = new WKTReader();
                Geometry point = reader.read("POINT(500 505)");
                Geometry point2 = reader.read("POINT (490 490)");
                Geometry line = reader.read("LINESTRING(550 551, 525 512, 565 566)");
                Geometry polygon = reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))");
                Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");


                ShapeWriter sw = new ShapeWriter();
                Shape pointShape = sw.toShape(point);
                Shape pointShape2 = sw.toShape(point2);
                Shape lineShape = sw.toShape(line);
                Shape polygonShape = sw.toShape(polygon);
                Shape searchShape = sw.toShape(search);

                g2d.setColor(Color.RED);
                g2d.draw(pointShape);
                g2d.draw(pointShape2);
                g2d.draw(lineShape);
                g2d.draw(polygonShape);

                g2d.setColor(Color.GREEN);
                g2d.draw(searchShape);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
