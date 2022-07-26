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

package org.dizitart.no2.common.util;

import java.util.Arrays;

/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
public class SpatialKey {
    private final long id;
    private final float[] minMax;

    public SpatialKey(long id, float... minMax) {
        this.id = id;
        this.minMax = minMax;
    }

    public float min(int dim) {
        return minMax[dim + dim];
    }

    public void setMin(int dim, float x) {
        minMax[dim + dim] = x;
    }

    public float max(int dim) {
        return minMax[dim + dim + 1];
    }

    public void setMax(int dim, float x) {
        minMax[dim + dim + 1] = x;
    }

    public long getId() {
        return id;
    }

    public boolean isNull() {
        return minMax.length == 0;
    }

    @Override
    public int hashCode() {
        return (int) ((id >>> 32) ^ id);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof SpatialKey)) {
            return false;
        }
        SpatialKey o = (SpatialKey) other;
        if (id != o.id) {
            return false;
        }
        return equalsIgnoringId(o);
    }

    public boolean equalsIgnoringId(SpatialKey o) {
        return Arrays.equals(minMax, o.minMax);
    }
}
