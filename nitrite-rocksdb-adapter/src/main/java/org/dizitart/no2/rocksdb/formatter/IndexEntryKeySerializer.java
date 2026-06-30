/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.IndexEntryKey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Order-preserving key serializer for the non-unique index {@link IndexEntryKey} composite-key
 * layout (issue #1260). RocksDB orders keys purely by their serialized bytes, so the encoding
 * must sort identically to {@link IndexEntryKey#compareTo}: first by the indexed value, then by
 * the bracket marker, then by the trailing id.
 *
 * <p>The byte layout is {@code [value][bound][id?]}:
 * <ul>
 *   <li>{@code value} - a type-tagged, order-preserving, prefix-free encoding of the indexed
 *       value (a length-prefix would sort by length first and break value ordering, so
 *       variable-length values are 0x00-terminated with escaping instead);</li>
 *   <li>{@code bound} - one byte {@code 0x00/0x01/0x02} for the lower-bracket / exact / upper-
 *       bracket marker, so the brackets sort immediately before and after the {@code (value, *)}
 *       group;</li>
 *   <li>{@code id} - present only for exact keys: the id as a sign-flipped big-endian long.</li>
 * </ul>
 *
 * @author Anindya Chatterjee
 * @since 4.4
 */
public class IndexEntryKeySerializer extends KryoKeySerializer<IndexEntryKey> {
    // value type tags, ordered so DBNull sorts first (matching DBNull.compareTo)
    private static final int TAG_NULL = 0x00;
    private static final int TAG_BOOL = 0x10;
    private static final int TAG_NUMBER = 0x20;
    private static final int TAG_DATE = 0x30;
    private static final int TAG_STRING = 0x40;
    private static final int TAG_OTHER = 0xF0;

    private static final int BOUND_LOWER = 0x00;
    private static final int BOUND_EXACT = 0x01;
    private static final int BOUND_UPPER = 0x02;

    @Override
    public void write(Kryo kryo, Output output, IndexEntryKey object) {
        writeKey(kryo, output, object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IndexEntryKey read(Kryo kryo, Input input, Class<? extends IndexEntryKey> type) {
        return readKey(kryo, input, (Class<IndexEntryKey>) type);
    }

    @Override
    public void writeKey(Kryo kryo, Output output, IndexEntryKey object) {
        writeValue(output, object.getValue());
        switch (object.getBound()) {
            case IndexEntryKey.LOWER:
                output.writeByte(BOUND_LOWER);
                break;
            case IndexEntryKey.UPPER:
                output.writeByte(BOUND_UPPER);
                break;
            default:
                output.writeByte(BOUND_EXACT);
                writeOrderedLong(output, object.getNitriteId().getIdValue());
                break;
        }
    }

    @Override
    public IndexEntryKey readKey(Kryo kryo, Input input, Class<IndexEntryKey> type) {
        DBValue value = readValue(input);
        int bound = input.readByte() & 0xFF;
        switch (bound) {
            case BOUND_LOWER:
                return IndexEntryKey.lowerBound(value);
            case BOUND_UPPER:
                return IndexEntryKey.upperBound(value);
            default:
                long id = readOrderedLong(input);
                return IndexEntryKey.exact(value, NitriteId.createId(id));
        }
    }

    private void writeValue(Output output, DBValue dbValue) {
        Comparable<?> value = dbValue instanceof DBNull ? null : dbValue.getValue();
        if (value == null) {
            output.writeByte(TAG_NULL);
        } else if (value instanceof Boolean) {
            output.writeByte(TAG_BOOL);
            output.writeByte(((Boolean) value) ? 1 : 0);
        } else if (value instanceof Double) {
            // DBValue normalizes every Number to Double
            output.writeByte(TAG_NUMBER);
            writeOrderedDouble(output, (Double) value);
        } else if (value instanceof Date) {
            output.writeByte(TAG_DATE);
            writeOrderedLong(output, ((Date) value).getTime());
        } else if (value instanceof String) {
            output.writeByte(TAG_STRING);
            writeOrderedBytes(output, ((String) value).getBytes(StandardCharsets.UTF_8));
        } else {
            // best effort for uncommon comparable types: prefix-free (equality lookups work),
            // ordering follows the java-serialized bytes
            output.writeByte(TAG_OTHER);
            writeOrderedBytes(output, javaSerialize(value));
        }
    }

    private DBValue readValue(Input input) {
        int tag = input.readByte() & 0xFF;
        switch (tag) {
            case TAG_NULL:
                return DBNull.getInstance();
            case TAG_BOOL:
                return new DBValue(input.readByte() != 0);
            case TAG_NUMBER:
                return new DBValue(readOrderedDouble(input));
            case TAG_DATE:
                return new DBValue(new Date(readOrderedLong(input)));
            case TAG_STRING:
                return new DBValue(new String(readOrderedBytes(input), StandardCharsets.UTF_8));
            case TAG_OTHER:
                return new DBValue((Comparable<?>) javaDeserialize(readOrderedBytes(input)));
            default:
                throw new NitriteIOException("Unknown index key value tag: " + tag);
        }
    }

    // ---- order-preserving primitives -------------------------------------------------------

    /**
     * Writes a long as 8 big-endian bytes with the sign bit flipped, so an unsigned byte
     * comparison reproduces the signed numeric order.
     */
    private void writeOrderedLong(Output output, long value) {
        long v = value ^ Long.MIN_VALUE;
        for (int shift = 56; shift >= 0; shift -= 8) {
            output.writeByte((int) ((v >>> shift) & 0xFF));
        }
    }

    private long readOrderedLong(Input input) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v = (v << 8) | (input.readByte() & 0xFFL);
        }
        return v ^ Long.MIN_VALUE;
    }

    /**
     * Encodes a double so that an unsigned byte comparison reproduces the numeric order: set the
     * sign bit for positives, flip every bit for negatives (the standard IEEE-754 total-order
     * transform).
     */
    private void writeOrderedDouble(Output output, double value) {
        long bits = Double.doubleToLongBits(value);
        bits ^= (bits >> 63) | Long.MIN_VALUE;
        for (int shift = 56; shift >= 0; shift -= 8) {
            output.writeByte((int) ((bits >>> shift) & 0xFF));
        }
    }

    private double readOrderedDouble(Input input) {
        long bits = 0;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | (input.readByte() & 0xFFL);
        }
        bits ^= ((~bits) >> 63) | Long.MIN_VALUE;
        return Double.longBitsToDouble(bits);
    }

    /**
     * Writes variable-length bytes 0x00-terminated, escaping any literal 0x00 as {@code 00 01}.
     * This keeps the encoding prefix-free (so distinct values never bracket each other) while
     * preserving byte order (a terminated value sorts before any value that continues).
     */
    private void writeOrderedBytes(Output output, byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0x00) {
                output.writeByte(0x00);
                output.writeByte(0x01);
            } else {
                output.writeByte(b);
            }
        }
        output.writeByte(0x00);
        output.writeByte(0x00);
    }

    private byte[] readOrderedBytes(Input input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int b = input.readByte() & 0xFF;
            if (b == 0x00) {
                int next = input.readByte() & 0xFF;
                if (next == 0x00) {
                    break;
                }
                buffer.write(0x00);
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    private byte[] javaSerialize(Object value) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to encode index key value", e);
        }
    }

    private Object javaDeserialize(byte[] bytes) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to decode index key value", e);
        }
    }
}
