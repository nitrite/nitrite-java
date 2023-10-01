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

package org.dizitart.no2.mvstore;

import org.dizitart.no2.common.util.SpatialKey;
import org.h2.mvstore.rtree.Spatial;

import java.util.Arrays;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class MVSpatialKey extends SpatialKey implements Spatial {
    private final float[] minMax;
    public MVSpatialKey(long id, float... minMax) {
        super(id, minMax);
        this.minMax = minMax;
    }

    @Override
    public Spatial clone(long id) {
        return new MVSpatialKey(id, this.minMax.clone());
    }

    @Override
    public boolean equalsIgnoringId(Spatial o) {
        return Arrays.equals(minMax, ((MVSpatialKey)o).minMax);
    }
}
