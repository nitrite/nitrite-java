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

package org.dizitart.no2.index;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;

import java.io.Serializable;

/**
 * Composite key for the non-unique index layout. Instead of storing every matching
 * {@link NitriteId} in a single ever-growing list keyed by the indexed value - which makes
 * each insert an O(n) read-modify-write of that list (O(n²) for a bulk load, see
 * <a href="https://github.com/nitrite/nitrite-java/issues/1260">issue #1260</a>) - the
 * non-unique index stores one row per {@code (value, id)} pair keyed by an
 * {@link IndexEntryKey}. Inserts and removals then become O(log n) point operations and an
 * equality lookup is a range scan over the {@code (value, *)} prefix.
 *
 * <p>The key orders first by the indexed {@code value}, then by the trailing {@code bound}
 * marker, then by the {@code id}. The {@code bound} marker lets a caller build the brackets
 * {@code (value, LOWER)} and {@code (value, UPPER)} that sort immediately before and after
 * every real {@code (value, id)} row, so the whole leading-value group can be range scanned.
 *
 * @author Anindya Chatterjee
 * @since 4.4
 */
public class IndexEntryKey implements Comparable<IndexEntryKey>, Serializable {
    private static final long serialVersionUID = 1L;

    public static final byte LOWER = -1;
    public static final byte EXACT = 0;
    public static final byte UPPER = 1;

    private final DBValue value;
    private final byte bound;
    private final long id;

    IndexEntryKey(DBValue value, NitriteId nitriteId) {
        this.value = value == null ? DBNull.getInstance() : value;
        this.bound = EXACT;
        this.id = nitriteId.getIdValue();
    }

    private IndexEntryKey(DBValue value, byte bound) {
        this.value = value == null ? DBNull.getInstance() : value;
        this.bound = bound;
        this.id = 0;
    }

    /**
     * Rebuilds an exact {@code (value, id)} key. Used by store adapters that decode keys back
     * from their persisted form.
     */
    public static IndexEntryKey exact(DBValue value, NitriteId nitriteId) {
        return new IndexEntryKey(value, nitriteId);
    }

    /**
     * The bracket that sorts immediately before every {@code (value, id)} row of {@code value}.
     */
    public static IndexEntryKey lowerBound(DBValue value) {
        return new IndexEntryKey(value, LOWER);
    }

    /**
     * The bracket that sorts immediately after every {@code (value, id)} row of {@code value}.
     */
    public static IndexEntryKey upperBound(DBValue value) {
        return new IndexEntryKey(value, UPPER);
    }

    /**
     * The indexed value component of this key.
     */
    public DBValue getValue() {
        return value;
    }

    /**
     * The bracket marker of this key: {@link #LOWER}, {@link #EXACT} or {@link #UPPER}.
     */
    public byte getBound() {
        return bound;
    }

    /**
     * The {@link NitriteId} component of this key, or {@code null} for a bracket key.
     */
    public NitriteId getNitriteId() {
        return bound == EXACT ? NitriteId.createId(id) : null;
    }

    @Override
    public int compareTo(IndexEntryKey other) {
        int cmp = value.compareTo(other.value);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Byte.compare(bound, other.bound);
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntryKey)) return false;
        IndexEntryKey that = (IndexEntryKey) o;
        return bound == that.bound && id == that.id && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + bound;
        result = 31 * result + Long.hashCode(id);
        return result;
    }

    @Override
    public String toString() {
        return "IndexEntryKey{value=" + value + ", bound=" + bound + ", id=" + id + '}';
    }
}
