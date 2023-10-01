/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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


/**
 * An interface to define methods for encoding and decoding objects and their
 * keys.
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
public interface ObjectFormatter {
    /**
     * Encodes an object into a byte array.
     *
     * @param object the object to encode
     * @param <T>    the type of the object
     * @return the byte array representing the encoded object
     */
    <T> byte[] encode(T object);

    /**
     * Encodes an object's key into a byte array.
     *
     * @param object the object whose key to encode
     * @param <T>    the type of the object
     * @return the byte array representing the encoded key
     */
    <T> byte[] encodeKey(T object);

    /**
     * Decodes a byte array into an object of the specified type.
     *
     * @param bytes the byte array to decode
     * @param type  the type of the object to decode
     * @param <T>   the type of the object
     * @return the decoded object
     */
    <T> T decode(byte[] bytes, Class<T> type);

    /**
     * Decodes a byte array into an object's key of the specified type.
     *
     * @param bytes the byte array to decode
     * @param type  the type of the object's key to decode
     * @param <T>   the type of the object's key
     * @return the decoded object's key
     */
    <T> T decodeKey(byte[] bytes, Class<T> type);
}
