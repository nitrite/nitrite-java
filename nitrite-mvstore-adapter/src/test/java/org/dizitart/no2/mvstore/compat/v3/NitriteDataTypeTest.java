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

package org.dizitart.no2.mvstore.compat.v3;

import org.h2.mvstore.WriteBuffer;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.*;

public class NitriteDataTypeTest {
    @Test
    public void testAutoDetectDataTypeGetType() {
        assertTrue((new NitriteDataType.BigDecimalType(new NitriteDataType()))
                .getType("42") instanceof NitriteDataType.StringType);
        assertEquals(4,
                (new NitriteDataType.BigDecimalType(new NitriteDataType())).getType(0).typeId);
    }

    @Test
    public void testBigDecimalTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.BigDecimalType actualBigDecimalType = new NitriteDataType.BigDecimalType(nitriteDataType);
        assertEquals(9, actualBigDecimalType.typeId);
        assertSame(actualBigDecimalType.base, nitriteDataType);
    }

    @Test
    public void testBigIntegerTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.BigIntegerType actualBigIntegerType = new NitriteDataType.BigIntegerType(nitriteDataType);
        assertEquals(6, actualBigIntegerType.typeId);
        assertSame(actualBigIntegerType.base, nitriteDataType);
    }

    @Test
    public void testBooleanTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.BooleanType actualBooleanType = new NitriteDataType.BooleanType(nitriteDataType);
        assertEquals(1, actualBooleanType.typeId);
        assertSame(actualBooleanType.base, nitriteDataType);
    }

    @Test
    public void testByteTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.ByteType actualByteType = new NitriteDataType.ByteType(nitriteDataType);
        assertEquals(2, actualByteType.typeId);
        assertSame(actualByteType.base, nitriteDataType);
    }

    @Test
    public void testCharacterTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.CharacterType actualCharacterType = new NitriteDataType.CharacterType(nitriteDataType);
        assertEquals(10, actualCharacterType.typeId);
        assertSame(actualCharacterType.base, nitriteDataType);
    }

    @Test
    public void testDateTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.DateType actualDateType = new NitriteDataType.DateType(nitriteDataType);
        assertEquals(13, actualDateType.typeId);
        assertSame(actualDateType.base, nitriteDataType);
    }

    @Test
    public void testDoubleTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.DoubleType actualDoubleType = new NitriteDataType.DoubleType(nitriteDataType);
        assertEquals(8, actualDoubleType.typeId);
        assertSame(actualDoubleType.base, nitriteDataType);
    }

    @Test
    public void testFloatTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.FloatType actualFloatType = new NitriteDataType.FloatType(nitriteDataType);
        assertEquals(7, actualFloatType.typeId);
        assertSame(actualFloatType.base, nitriteDataType);
    }

    @Test
    public void testIntegerTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.IntegerType actualIntegerType = new NitriteDataType.IntegerType(nitriteDataType);
        assertEquals(4, actualIntegerType.typeId);
        assertSame(actualIntegerType.base, nitriteDataType);
    }

    @Test
    public void testIsBigInteger() {
        assertFalse(NitriteDataType.isBigInteger("Obj"));
        assertFalse(NitriteDataType.isBigInteger(null));
    }

    @Test
    public void testIsBigDecimal() {
        assertFalse(NitriteDataType.isBigDecimal("Obj"));
        assertFalse(NitriteDataType.isBigDecimal(null));
    }

    @Test
    public void testIsDate() {
        assertFalse(NitriteDataType.isDate("Obj"));
        assertFalse(NitriteDataType.isDate(null));
    }

    @Test
    public void testIsArray() {
        assertFalse(NitriteDataType.isArray("Obj"));
        assertFalse(NitriteDataType.isArray(null));
    }

    @Test
    public void testGetCommonClassId() {
        assertEquals(8, NitriteDataType.getCommonClassId(Object.class).intValue());
        assertNull(NitriteDataType.getCommonClassId(null));
    }

    @Test
    public void testLongTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.LongType actualLongType = new NitriteDataType.LongType(nitriteDataType);
        assertEquals(5, actualLongType.typeId);
        assertSame(actualLongType.base, nitriteDataType);
    }

    @Test
    public void testNullTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.NullType actualNullType = new NitriteDataType.NullType(nitriteDataType);
        assertEquals(0, actualNullType.typeId);
        assertSame(actualNullType.base, nitriteDataType);
    }

    @Test
    public void testObjectArrayTypeConstructor() {
        assertEquals(14, (new NitriteDataType.ObjectArrayType(new NitriteDataType())).typeId);
    }

    @Test
    public void testSerialize() {
        byte[] actualSerializeResult = NitriteDataType.serialize("Obj");
        assertEquals(10, actualSerializeResult.length);
        assertEquals((byte) 0, actualSerializeResult[2]);
        assertEquals((byte) 5, actualSerializeResult[3]);
        assertEquals('t', actualSerializeResult[4]);
        assertEquals((byte) 0, actualSerializeResult[5]);
        assertEquals((byte) 3, actualSerializeResult[6]);
        assertEquals('O', actualSerializeResult[7]);
        assertEquals('b', actualSerializeResult[8]);
        assertEquals('j', actualSerializeResult[9]);
    }

    @Test
    public void testCompareNotNull() throws UnsupportedEncodingException {
        byte[] data1 = "AAAAAAAA".getBytes("UTF-8");
        assertEquals(0, NitriteDataType.compareNotNull(data1, "AAAAAAAA".getBytes("UTF-8")));
    }

    @Test
    public void testCompareNotNull2() throws UnsupportedEncodingException {
        assertEquals(-1,
                NitriteDataType.compareNotNull(new byte[]{0, 'A', 'A', 'A', 'A', 'A', 'A', 'A'}, "AAAAAAAA".getBytes("UTF-8")));
    }

    @Test
    public void testCompareNotNull3() throws UnsupportedEncodingException {
        byte[] data1 = "￿AAAAAAA".getBytes("UTF-8");
        assertEquals(1, NitriteDataType.compareNotNull(data1, "AAAAAAAA".getBytes("UTF-8")));
    }

    @Test
    public void testAutoDetectDataTypeCompare() {
        assertEquals(-1, (new NitriteDataType.BigDecimalType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.BigDecimalType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testBigDecimalTypeCompare() {
        assertEquals(-1, (new NitriteDataType.BigDecimalType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.BigDecimalType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testBigIntegerTypeCompare() {
        assertEquals(-1, (new NitriteDataType.BigIntegerType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.BigIntegerType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testBooleanTypeCompare() {
        assertEquals(-1, (new NitriteDataType.BooleanType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.BooleanType(new NitriteDataType())).compare(true, "B Obj"));
        assertEquals(0, (new NitriteDataType.BooleanType(new NitriteDataType())).compare(true, true));
    }

    @Test
    public void testByteTypeCompare() {
        assertEquals(-1, (new NitriteDataType.ByteType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.ByteType(new NitriteDataType())).compare((byte) 'A', "B Obj"));
        assertEquals(0, (new NitriteDataType.ByteType(new NitriteDataType())).compare((byte) 'A', (byte) 'A'));
    }

    @Test
    public void testCharacterTypeCompare() {
        assertEquals(-1, (new NitriteDataType.CharacterType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.CharacterType(new NitriteDataType())).compare('\u0000', "B Obj"));
        assertEquals(0, (new NitriteDataType.CharacterType(new NitriteDataType())).compare('\u0000', '\u0000'));
    }

    @Test
    public void testCompare() {
        assertEquals(0, (new NitriteDataType()).compare("42", "42"));
        assertEquals(-1, (new NitriteDataType()).compare(0, "42"));
        assertEquals(1, (new NitriteDataType()).compare("42", 0));
        assertEquals(0, (new NitriteDataType()).compare(0, 0));
    }

    @Test
    public void testDateTypeCompare() {
        assertEquals(-1, (new NitriteDataType.DateType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.DateType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testDoubleTypeCompare() {
        assertEquals(-1, (new NitriteDataType.DoubleType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.DoubleType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(-1, (new NitriteDataType.DoubleType(new NitriteDataType())).compare(10.0, "B Obj"));
        assertEquals(0, (new NitriteDataType.DoubleType(new NitriteDataType())).compare(10.0, 10.0));
    }

    @Test
    public void testFloatTypeCompare() {
        assertEquals(-1, (new NitriteDataType.FloatType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.FloatType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(-1, (new NitriteDataType.FloatType(new NitriteDataType())).compare(10.0f, "B Obj"));
        assertEquals(0, (new NitriteDataType.FloatType(new NitriteDataType())).compare(10.0f, 10.0f));
    }

    @Test
    public void testIntegerTypeCompare() {
        assertEquals(-1, (new NitriteDataType.IntegerType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.IntegerType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(0, (new NitriteDataType.IntegerType(new NitriteDataType())).compare(0, 0));
    }

    @Test
    public void testLongTypeCompare() {
        assertEquals(-1, (new NitriteDataType.LongType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.LongType(new NitriteDataType())).compare(0L, "B Obj"));
        assertEquals(0, (new NitriteDataType.LongType(new NitriteDataType())).compare(0L, 0L));
    }

    @Test
    public void testNullTypeCompare() {
        assertEquals(-1, (new NitriteDataType.NullType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.NullType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(-1, (new NitriteDataType.NullType(new NitriteDataType())).compare(null, "B Obj"));
        assertEquals(1, (new NitriteDataType.NullType(new NitriteDataType())).compare("A Obj", null));
        assertEquals(0, (new NitriteDataType.NullType(new NitriteDataType())).compare(null, null));
    }

    @Test
    public void testObjectArrayTypeCompare() {
        assertEquals(-1, (new NitriteDataType.ObjectArrayType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.ObjectArrayType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testSerializedObjectTypeCompare() {
        assertEquals(-1, (new NitriteDataType.SerializedObjectType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.SerializedObjectType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(0, (new NitriteDataType.SerializedObjectType(new NitriteDataType())).compare(0, 0));
    }

    @Test
    public void testShortTypeCompare() {
        assertEquals(-1, (new NitriteDataType.ShortType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.ShortType(new NitriteDataType())).compare((short) 0, "B Obj"));
        assertEquals(0, (new NitriteDataType.ShortType(new NitriteDataType())).compare((short) 0, (short) 0));
    }

    @Test
    public void testStringTypeCompare() {
        assertEquals(-1, (new NitriteDataType.StringType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.StringType(new NitriteDataType())).compare(0, "B Obj"));
        assertEquals(1, (new NitriteDataType.StringType(new NitriteDataType())).compare("A Obj", 0));
        assertEquals(-1, (new NitriteDataType.StringType(new NitriteDataType())).compare(4, "B Obj"));
        assertEquals(0, (new NitriteDataType.StringType(new NitriteDataType())).compare(0, 0));
    }

    @Test
    public void testUUIDTypeCompare() {
        assertEquals(-1, (new NitriteDataType.UUIDType(new NitriteDataType())).compare("A Obj", "B Obj"));
        assertEquals(-1, (new NitriteDataType.UUIDType(new NitriteDataType())).compare(0, "B Obj"));
    }

    @Test
    public void testUUIDTypeCompare2() {
        NitriteDataType.UUIDType uuidType = new NitriteDataType.UUIDType(new NitriteDataType());
        assertEquals(1, uuidType.compare(UUID.randomUUID(), "B Obj"));
    }

    @Test
    public void testAutoDetectDataTypeGetMemory() {
        assertEquals(28, (new NitriteDataType.BigDecimalType(new NitriteDataType())).getMemory("42"));
        assertEquals(24, (new NitriteDataType.BigDecimalType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testBigDecimalTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.BigDecimalType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.BigDecimalType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testBigDecimalTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, (new NitriteDataType.BigDecimalType(nitriteDataType)).getMemory("Obj"));
    }

    @Test
    public void testBigIntegerTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.BigIntegerType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.BigIntegerType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testBigIntegerTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, (new NitriteDataType.BigIntegerType(nitriteDataType)).getMemory("Obj"));
    }

    @Test
    public void testBooleanTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.BooleanType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(0, (new NitriteDataType.BooleanType(new NitriteDataType())).getMemory(true));
        assertEquals(24, (new NitriteDataType.BooleanType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testByteTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.ByteType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(0, (new NitriteDataType.ByteType(new NitriteDataType())).getMemory((byte) 'A'));
        assertEquals(24, (new NitriteDataType.ByteType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testCharacterTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.CharacterType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.CharacterType(new NitriteDataType())).getMemory('\u0000'));
        assertEquals(24, (new NitriteDataType.CharacterType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testDateTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.DateType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.DateType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testDateTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, (new NitriteDataType.DateType(nitriteDataType)).getMemory("Obj"));
    }

    @Test
    public void testDoubleTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.DoubleType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.DoubleType(new NitriteDataType())).getMemory(0));
        assertEquals(30, (new NitriteDataType.DoubleType(new NitriteDataType())).getMemory(10.0));
    }

    @Test
    public void testFloatTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.FloatType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.FloatType(new NitriteDataType())).getMemory(0));
        assertEquals(24, (new NitriteDataType.FloatType(new NitriteDataType())).getMemory(10.0f));
    }

    @Test
    public void testGetMemory() {
        assertEquals(30, (new NitriteDataType()).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType()).getMemory(0));
    }

    @Test
    public void testGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, nitriteDataType.getMemory("Obj"));
    }

    @Test
    public void testIntegerTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.IntegerType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.IntegerType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testIntegerTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, (new NitriteDataType.IntegerType(nitriteDataType)).getMemory("Obj"));
    }

    @Test
    public void testLongTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.LongType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(30, (new NitriteDataType.LongType(new NitriteDataType())).getMemory(0L));
        assertEquals(24, (new NitriteDataType.LongType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testNullTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.NullType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.NullType(new NitriteDataType())).getMemory(0));
        assertEquals(0, (new NitriteDataType.NullType(new NitriteDataType())).getMemory(null));
    }

    @Test
    public void testObjectArrayTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.ObjectArrayType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.ObjectArrayType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testObjectArrayTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(30, (new NitriteDataType.ObjectArrayType(nitriteDataType)).getMemory("Obj"));
    }

    @Test
    public void testSerializedObjectTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.SerializedObjectType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.SerializedObjectType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testSerializedObjectTypeGetMemory2() {
        NitriteDataType.SerializedObjectType serializedObjectType = new NitriteDataType.SerializedObjectType(
                new NitriteDataType());
        serializedObjectType.write(new WriteBuffer(), 19088743);
        assertEquals(30, serializedObjectType.getMemory("Obj"));
    }

    @Test
    public void testShortTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.ShortType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.ShortType(new NitriteDataType())).getMemory((short) 0));
        assertEquals(24, (new NitriteDataType.ShortType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testStringTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.StringType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.StringType(new NitriteDataType())).getMemory(0));
        assertEquals(24, (new NitriteDataType.StringType(new NitriteDataType())).getMemory(4));
    }

    @Test
    public void testStringTypeGetMemory2() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        nitriteDataType.switchType(1);
        assertEquals(24, (new NitriteDataType.StringType(nitriteDataType)).getMemory(0));
    }

    @Test
    public void testUUIDTypeGetMemory() {
        assertEquals(30, (new NitriteDataType.UUIDType(new NitriteDataType())).getMemory("Obj"));
        assertEquals(24, (new NitriteDataType.UUIDType(new NitriteDataType())).getMemory(0));
    }

    @Test
    public void testUUIDTypeGetMemory2() {
        NitriteDataType.UUIDType uuidType = new NitriteDataType.UUIDType(new NitriteDataType());
        assertEquals(40, uuidType.getMemory(UUID.randomUUID()));
    }

    @Test
    public void testBigDecimalTypeRead() {
        Object actualReadResult = (new NitriteDataType.BigDecimalType(new NitriteDataType())).read(null, 46);
        assertSame(((BigDecimal) actualReadResult).ZERO, actualReadResult);
    }

    @Test
    public void testBigDecimalTypeRead2() {
        Object actualReadResult = (new NitriteDataType.BigDecimalType(new NitriteDataType())).read(null, 47);
        assertSame(((BigDecimal) actualReadResult).ONE, actualReadResult);
    }

    @Test
    public void testBigIntegerTypeRead() {
        Object actualReadResult = (new NitriteDataType.BigIntegerType(new NitriteDataType())).read(null, 37);
        assertSame(((BigInteger) actualReadResult).ZERO, actualReadResult);
    }

    @Test
    public void testBigIntegerTypeRead2() {
        Object actualReadResult = (new NitriteDataType.BigIntegerType(new NitriteDataType())).read(null, 38);
        assertSame(((BigInteger) actualReadResult).ONE, actualReadResult);
    }

    @Test
    public void testNullTypeRead() {
        assertNull((new NitriteDataType.NullType(new NitriteDataType())).read(null, 1));
    }

    @Test
    public void testStringTypeRead2() {
        assertEquals("", (new NitriteDataType.StringType(new NitriteDataType())).read(null, 88));
    }

    @Test
    public void testAutoDetectDataTypeWrite() {
        NitriteDataType.BigDecimalType bigDecimalType = new NitriteDataType.BigDecimalType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        bigDecimalType.write(writeBuffer, "42");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testAutoDetectDataTypeWrite2() {
        NitriteDataType.BigDecimalType bigDecimalType = new NitriteDataType.BigDecimalType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        bigDecimalType.write(writeBuffer, new Object[]{"42", "42", "42"}, 3, true);
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testAutoDetectDataTypeWrite3() {
        NitriteDataType.BigDecimalType bigDecimalType = new NitriteDataType.BigDecimalType(null);
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> bigDecimalType.write(new WriteBuffer(), new Object[]{}, 3, true));
    }

    @Test
    public void testBigDecimalTypeWrite() {
        NitriteDataType.BigDecimalType bigDecimalType = new NitriteDataType.BigDecimalType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        bigDecimalType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testBigIntegerTypeWrite() {
        NitriteDataType.BigIntegerType bigIntegerType = new NitriteDataType.BigIntegerType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        bigIntegerType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testBooleanTypeWrite() {
        NitriteDataType.BooleanType booleanType = new NitriteDataType.BooleanType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        booleanType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testByteTypeWrite() {
        NitriteDataType.ByteType byteType = new NitriteDataType.ByteType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        byteType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testCharacterTypeWrite() {
        NitriteDataType.CharacterType characterType = new NitriteDataType.CharacterType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        characterType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testDateTypeWrite() {
        NitriteDataType.DateType dateType = new NitriteDataType.DateType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        dateType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testDoubleTypeWrite() {
        NitriteDataType.DoubleType doubleType = new NitriteDataType.DoubleType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        doubleType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testFloatTypeWrite() {
        NitriteDataType.FloatType floatType = new NitriteDataType.FloatType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        floatType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testIntegerTypeWrite() {
        NitriteDataType.IntegerType integerType = new NitriteDataType.IntegerType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        integerType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testLongTypeWrite() {
        NitriteDataType.LongType longType = new NitriteDataType.LongType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        longType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testNullTypeWrite() {
        NitriteDataType.NullType nullType = new NitriteDataType.NullType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        nullType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testObjectArrayTypeWrite() {
        NitriteDataType.ObjectArrayType objectArrayType = new NitriteDataType.ObjectArrayType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        objectArrayType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testSerializedObjectTypeWrite() {
        NitriteDataType.SerializedObjectType serializedObjectType = new NitriteDataType.SerializedObjectType(
                new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        serializedObjectType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testShortTypeWrite() {
        NitriteDataType.ShortType shortType = new NitriteDataType.ShortType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        shortType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testStringTypeWrite() {
        NitriteDataType.StringType stringType = new NitriteDataType.StringType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        stringType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testUUIDTypeWrite() {
        NitriteDataType.UUIDType uuidType = new NitriteDataType.UUIDType(new NitriteDataType());
        WriteBuffer writeBuffer = new WriteBuffer();
        uuidType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testSerializedObjectTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.SerializedObjectType actualSerializedObjectType = new NitriteDataType.SerializedObjectType(
                nitriteDataType);
        assertEquals(19, actualSerializedObjectType.typeId);
        assertSame(actualSerializedObjectType.base, nitriteDataType);
    }

    @Test
    public void testShortTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.ShortType actualShortType = new NitriteDataType.ShortType(nitriteDataType);
        assertEquals(3, actualShortType.typeId);
        assertSame(actualShortType.base, nitriteDataType);
    }

    @Test
    public void testStringTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.StringType actualStringType = new NitriteDataType.StringType(nitriteDataType);
        assertEquals(11, actualStringType.typeId);
        assertSame(actualStringType.base, nitriteDataType);
    }

    @Test
    public void testUUIDTypeConstructor() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        NitriteDataType.UUIDType actualUuidType = new NitriteDataType.UUIDType(nitriteDataType);
        assertEquals(12, actualUuidType.typeId);
        assertSame(actualUuidType.base, nitriteDataType);
    }

    @Test
    public void testWrite() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        WriteBuffer writeBuffer = new WriteBuffer();
        nitriteDataType.write(writeBuffer, "Obj");
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testWrite4() {
        NitriteDataType nitriteDataType = new NitriteDataType();
        WriteBuffer writeBuffer = new WriteBuffer();
        nitriteDataType.write(writeBuffer, new Object[]{"42", "42", "42"}, 3, true);
        assertEquals(1048576, writeBuffer.capacity());
    }

    @Test
    public void testWrite5() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> (new NitriteDataType()).write(null, new Object[]{}, 3, true));
    }

    @Test
    public void testSwitchType() {
        assertTrue((new NitriteDataType()).switchType("Obj") instanceof NitriteDataType.StringType);
        assertEquals(4, (new NitriteDataType()).switchType(0).typeId);
    }
}

