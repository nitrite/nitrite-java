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

import java.io.Serializable;

/**
 * Represents a bounding box for spatial indexing.
 *
 * @author Anindya Chatterjee
 */
public interface BoundingBox extends Serializable {
    /**
     * Returns the minimum x-coordinate of the bounding box.
     *
     * @return the minimum x-coordinate of the bounding box.
     */
    float getMinX();

    /**
     * Returns the maximum x-coordinate of the bounding box.
     *
     * @return the maximum x-coordinate of the bounding box
     */
    float getMaxX();

    /**
     * Returns the minimum y-coordinate of the bounding box.
     *
     * @return the minimum y-coordinate of the bounding box
     */
    float getMinY();

    /**
     * Returns the maximum Y coordinate of the bounding box.
     *
     * @return the maximum Y coordinate of the bounding box.
     */
    float getMaxY();
}
