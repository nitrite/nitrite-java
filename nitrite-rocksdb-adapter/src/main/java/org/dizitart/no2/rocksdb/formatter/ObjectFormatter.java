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
 * @author Anindya Chatterjee
 */
public interface ObjectFormatter {
    <T> byte[] encode(T object);
    <T> byte[] encodeKey(T object);

    <T> T decode(byte[] bytes, Class<T> type);
    <T> T decodeKey(byte[] bytes, Class<T> type);
}
