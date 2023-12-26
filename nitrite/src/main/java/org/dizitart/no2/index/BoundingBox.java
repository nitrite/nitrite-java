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

package org.dizitart.no2.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a bounding box for spatial indexing.
 *
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox implements Serializable {
    private static final long serialVersionUID = 1703439026L;

    /**
     * An empty bounding box.
     */
    public static final BoundingBox EMPTY = new BoundingBox(0, 0, 0, 0);

    /**
     * Returns the minimum x-coordinate of the bounding box.
     *
     * @return the minimum x-coordinate of the bounding box.
     */
    private float minX;

    /**
     * Returns the maximum x-coordinate of the bounding box.
     *
     * @return the maximum x-coordinate of the bounding box
     */
    private float maxX;

    /**
     * Returns the minimum y-coordinate of the bounding box.
     *
     * @return the minimum y-coordinate of the bounding box
     */
    private float minY;

    /**
     * Returns the maximum Y coordinate of the bounding box.
     *
     * @return the maximum Y coordinate of the bounding box.
     */
    private float maxY;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeFloat(minX);
        stream.writeFloat(maxX);
        stream.writeFloat(minY);
        stream.writeFloat(maxY);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        this.minX = stream.readFloat();
        this.maxX = stream.readFloat();
        this.minY = stream.readFloat();
        this.maxY = stream.readFloat();
    }
}
