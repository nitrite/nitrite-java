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

package org.dizitart.no2.common.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class Base64Test {
    @Test
    public void testDecode() throws UnsupportedEncodingException {
        byte[] actualDecodeResult = Base64.decode("AAAAAAAA".getBytes("UTF-8"), 1);
        assertEquals(6, actualDecodeResult.length);
        assertEquals((byte) 0, actualDecodeResult[0]);
        assertEquals((byte) 0, actualDecodeResult[1]);
        assertEquals((byte) 0, actualDecodeResult[2]);
        assertEquals((byte) 0, actualDecodeResult[3]);
        assertEquals((byte) 0, actualDecodeResult[4]);
        assertEquals((byte) 0, actualDecodeResult[5]);
    }

    @Test
    public void testDecode2() {
        byte[] actualDecodeResult = Base64.decode(new byte[]{0, 'A', 'A', 'A', 'A', 'A', 'A', 'A'}, 1);
        assertEquals(5, actualDecodeResult.length);
        assertEquals((byte) 0, actualDecodeResult[0]);
        assertEquals((byte) 0, actualDecodeResult[1]);
        assertEquals((byte) 0, actualDecodeResult[2]);
        assertEquals((byte) 0, actualDecodeResult[3]);
        assertEquals((byte) 0, actualDecodeResult[4]);
    }

    @Test
    public void testDecode3() throws UnsupportedEncodingException {
        byte[] actualDecodeResult = Base64.decode("AAAAAAAA".getBytes("UTF-8"), 2, 3, 1);
        assertEquals(2, actualDecodeResult.length);
        assertEquals((byte) 0, actualDecodeResult[0]);
        assertEquals((byte) 0, actualDecodeResult[1]);
    }

    @Test
    public void testDecode4() {
        assertThrows(IllegalArgumentException.class,
            () -> Base64.decode(new byte[]{'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A'}, 1, 1, 1));
    }

    @Test
    public void testDecode5() {
        byte[] actualDecodeResult = Base64.decode(new byte[]{'A', 'A', 0, 'A', 'A', 'A', 'A', 'A'}, 2, 3, 1);
        assertEquals(1, actualDecodeResult.length);
        assertEquals((byte) 0, actualDecodeResult[0]);
    }

    @Test
    public void testDecode7() {
        assertThrows(NegativeArraySizeException.class,
            () -> Base64.decode("AAAAAAAA".getBytes("UTF-8"), 2, Integer.MIN_VALUE, 1));
    }

    @Test
    public void testDecoderConstructor() throws UnsupportedEncodingException {
        assertEquals(24, (new Base64.Decoder(1, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"))).output.length);
        assertEquals(24, (new Base64.Decoder(8, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"))).output.length);
    }

    @Test
    public void testEncodeToString() throws UnsupportedEncodingException {
        assertEquals("QUFBQUFBQUE\n", Base64.encodeToString("AAAAAAAA".getBytes("UTF-8"), 1));
        assertEquals("", Base64.encodeToString(new byte[]{}, 1));
        assertEquals("QUFBQUFBQUFBQUFBQUFBQQ\n", Base64.encodeToString("AAAAAAAAAAAAAAAA".getBytes("UTF-8"), 1));
        assertEquals("QUFBQUFBQUE=\n", Base64.encodeToString("AAAAAAAA".getBytes("UTF-8"), 0));
        assertEquals("QUFBQUFBQUE=", Base64.encodeToString("AAAAAAAA".getBytes("UTF-8"), 2));
        assertEquals("QUFBQUFBQUE=\r\n", Base64.encodeToString("AAAAAAAA".getBytes("UTF-8"), 4));
        assertEquals("", Base64.encodeToString(new byte[]{}, 0));
    }

    @Test
    public void testEncode() throws UnsupportedEncodingException {
        assertEquals(12, Base64.encode("AAAAAAAA".getBytes("UTF-8"), 1).length);
        assertEquals(0, Base64.encode(new byte[]{}, 1).length);
        assertEquals(23, Base64.encode("AAAAAAAAAAAAAAAA".getBytes("UTF-8"), 1).length);
        assertEquals(13, Base64.encode("AAAAAAAA".getBytes("UTF-8"), 0).length);
        assertEquals(12, Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2).length);
        assertEquals(14, Base64.encode("AAAAAAAA".getBytes("UTF-8"), 4).length);
        assertEquals(0, Base64.encode(new byte[]{}, 0).length);
        assertEquals(0, Base64.encode(new byte[]{'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A'}, 1, 0, 1).length);
    }

    @Test
    public void testEncode2() throws UnsupportedEncodingException {
        byte[] actualEncodeResult = Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2, 3, 1);
        assertEquals(5, actualEncodeResult.length);
        assertEquals('Q', actualEncodeResult[0]);
        assertEquals('U', actualEncodeResult[1]);
        assertEquals('F', actualEncodeResult[2]);
        assertEquals('B', actualEncodeResult[3]);
        assertEquals((byte) 10, actualEncodeResult[4]);
    }

    @Test
    public void testEncode4() throws UnsupportedEncodingException {
        byte[] actualEncodeResult = Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2, 1, 1);
        assertEquals(3, actualEncodeResult.length);
        assertEquals('Q', actualEncodeResult[0]);
        assertEquals('Q', actualEncodeResult[1]);
        assertEquals((byte) 10, actualEncodeResult[2]);
    }

    @Test
    public void testEncode5() throws UnsupportedEncodingException {
        byte[] actualEncodeResult = Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2, 2, 1);
        assertEquals(4, actualEncodeResult.length);
        assertEquals('Q', actualEncodeResult[0]);
        assertEquals('U', actualEncodeResult[1]);
        assertEquals('E', actualEncodeResult[2]);
        assertEquals((byte) 10, actualEncodeResult[3]);
    }

    @Test
    public void testEncode6() throws UnsupportedEncodingException {
        byte[] actualEncodeResult = Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2, 1, 0);
        assertEquals(5, actualEncodeResult.length);
        assertEquals('Q', actualEncodeResult[0]);
        assertEquals('Q', actualEncodeResult[1]);
        assertEquals('=', actualEncodeResult[2]);
        assertEquals('=', actualEncodeResult[3]);
        assertEquals((byte) 10, actualEncodeResult[4]);
    }

    @Test
    public void testEncode8() throws UnsupportedEncodingException {
        byte[] actualEncodeResult = Base64.encode("AAAAAAAA".getBytes("UTF-8"), 2, 3, 4);
        assertEquals(6, actualEncodeResult.length);
        assertEquals('Q', actualEncodeResult[0]);
        assertEquals('U', actualEncodeResult[1]);
        assertEquals('F', actualEncodeResult[2]);
        assertEquals('B', actualEncodeResult[3]);
        assertEquals((byte) 13, actualEncodeResult[4]);
        assertEquals((byte) 10, actualEncodeResult[5]);
    }

    @Test
    public void testEncoderConstructor() throws UnsupportedEncodingException {
        Base64.Encoder actualEncoder = new Base64.Encoder(1, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"));
        assertFalse(actualEncoder.do_cr);
        assertEquals(0, actualEncoder.tailLen);
        assertEquals(24, actualEncoder.output.length);
        assertFalse(actualEncoder.do_padding);
        assertTrue(actualEncoder.do_newline);
    }

    @Test
    public void testEncoderConstructor2() throws UnsupportedEncodingException {
        Base64.Encoder actualEncoder = new Base64.Encoder(0, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"));
        assertFalse(actualEncoder.do_cr);
        assertEquals(0, actualEncoder.tailLen);
        assertEquals(24, actualEncoder.output.length);
        assertTrue(actualEncoder.do_padding);
        assertTrue(actualEncoder.do_newline);
    }

    @Test
    public void testEncoderConstructor3() throws UnsupportedEncodingException {
        Base64.Encoder actualEncoder = new Base64.Encoder(2, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"));
        assertFalse(actualEncoder.do_cr);
        assertEquals(0, actualEncoder.tailLen);
        assertEquals(24, actualEncoder.output.length);
        assertTrue(actualEncoder.do_padding);
        assertFalse(actualEncoder.do_newline);
    }

    @Test
    public void testEncoderConstructor4() throws UnsupportedEncodingException {
        Base64.Encoder actualEncoder = new Base64.Encoder(4, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"));
        assertTrue(actualEncoder.do_cr);
        assertEquals(0, actualEncoder.tailLen);
        assertEquals(24, actualEncoder.output.length);
        assertTrue(actualEncoder.do_padding);
        assertTrue(actualEncoder.do_newline);
    }

    @Test
    public void testEncoderConstructor5() throws UnsupportedEncodingException {
        Base64.Encoder actualEncoder = new Base64.Encoder(8, "AAAAAAAAAAAAAAAAAAAAAAAA".getBytes("UTF-8"));
        assertFalse(actualEncoder.do_cr);
        assertEquals(0, actualEncoder.tailLen);
        assertEquals(24, actualEncoder.output.length);
        assertTrue(actualEncoder.do_padding);
        assertTrue(actualEncoder.do_newline);
    }
}

