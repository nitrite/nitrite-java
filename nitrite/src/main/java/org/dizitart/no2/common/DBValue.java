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

package org.dizitart.no2.common;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.dizitart.no2.common.util.Comparables;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
public class DBValue implements Comparable<DBValue>, Serializable {
    private static final long serialVersionUID = 1617440702L;

    @Setter(AccessLevel.PRIVATE)
    private Comparable<?> value;

    private DBValue() {
    }

    public DBValue(Comparable<?> value) {
        this.value = normalizeNumber(value);
    }

    @Override
    public int compareTo(DBValue o) {
        if (o == null || o.value == null) {
            return 1;
        }

        if (value == null) {
            return -1;
        }

        return Comparables.compare(value, o.value);
    }

    private static Comparable<?> normalizeNumber(Comparable<?> value) {
        // Normalize numeric types to Double for consistent serialization
        // This ensures Integer(5) and Double(5.0) are treated the same in indexes
        if (value instanceof Number && !(value instanceof Double)) {
            double normalized = ((Number) value).doubleValue();
            // ...but only where a double can hold the value exactly. Beyond 2^53 it cannot,
            // and folding there maps distinct numbers onto one index key: consecutive longs
            // around 8.7e17 are 128 apart as doubles, so ids closer than that become the same
            // key, which makes a unique index reject a new id and a non-unique one return rows
            // belonging to a different id.
            if (isExactAsDouble((Number) value, normalized)) {
                return normalized;
            }
        }
        return value;
    }

    private static boolean isExactAsDouble(Number value, double normalized) {
        if (value instanceof Integer || value instanceof Short
            || value instanceof Byte || value instanceof Float) {
            // every value of these types survives the widening unchanged
            return true;
        }

        if (value instanceof Long) {
            // Casting the double back is exact for every double inside long range, so a value
            // that survives the round trip is one the double holds exactly. The range check is
            // what keeps Long.MAX_VALUE honest: its double rounds up to 2^63, and the cast back
            // saturates onto MAX_VALUE again, which would otherwise read as exact.
            long exact = value.longValue();
            return normalized >= -0x1p63 && normalized < 0x1p63 && (long) normalized == exact;
        }

        if (Double.isNaN(normalized) || Double.isInfinite(normalized)) {
            return false;
        }

        // new BigDecimal(double) is the exact value of the double, so this compares the
        // number against what the conversion actually produced
        BigDecimal converted = new BigDecimal(normalized);
        if (value instanceof BigInteger) {
            return converted.compareTo(new BigDecimal((BigInteger) value)) == 0;
        }
        if (value instanceof BigDecimal) {
            return converted.compareTo((BigDecimal) value) == 0;
        }
        return false;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(value);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.value = (Comparable<?>) stream.readObject();
    }
}
